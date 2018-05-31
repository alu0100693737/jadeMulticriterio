package jade;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lecturaFicheros.importanciaRelativaIndividual;
import lecturaFicheros.lectorProblema;

public class agenteTipoPromethee extends Agent {
	
	private lectorProblema datosProblema;
	private importanciaRelativaIndividual importanciaRelativa;

	private ArrayList<ArrayList<Float>> matrizIndicesPreferencia;
	private ArrayList<Float> prioridadesFinal;
	
	protected void setup() { 
		System.out.println("Creando el agente");
		System.out.println("\nHola! El agente "+ getAID().getName()+" está listo.\n");

		Object[] args = getArguments();         // Obtiene los argumentos dados en la inicialización del comprador
		if (args != null && args.length == 2) {  // Tiene que haber al menos un argumento
			if((args[0] instanceof lectorProblema) && (args[1] instanceof importanciaRelativaIndividual)) {	

				datosProblema = new lectorProblema((lectorProblema) args[0]);
				importanciaRelativa = new importanciaRelativaIndividual((importanciaRelativaIndividual) args[1]);
				System.out.println("\n------------------------------------------------------------------\n");
				
				addBehaviour(new comportamientoPrometheo());
				
				//Replica
				addBehaviour(new CyclicBehaviour() {
					
					@Override
					public void action() {
						// TODO Auto-generated method stub
						block();
						
						ACLMessage msg1 = receive();
						if(msg1 != null) {
							System.out.println("Recibido algo" + msg1.getContent());
						} else {
							block();
							System.out.println("Recibido 2");
						}
						
					}
				});

			} else {
				System.err.println("Tipos de datos de argumentos erroneo, ERROR");
				return;
			}
		} else {
			System.err.println("Recuerde que debe añadirse al agente el conjunto de datos y su importancia relativa personal, ERROR");
			return;
		}
	}
	class comportamientoPrometheo extends OneShotBehaviour {

		@Override
		public void action() {
			System.out.println("Comportamiento");
			matrizIndicesPreferencia = new ArrayList<ArrayList<Float>>();
			matrizIndicesPreferencias();
			
			//System.out.println("Matriz Indices Preferencias");
			//showMatrizIndicesPreferencia();
			
			//calculosFlujosPositivos();
			
			//calculosFlujosNegativos();
			
			calculosFlujoNeto(); //Se calcula el positivo, el negativo y el neto
			
			ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
			msg.setContent(getName() + "\n" + getPrioridadesFinal());
			msg.addReceiver(new AID("agenteModerador", AID.ISLOCALNAME));
			send(msg);
			
			block();
			
			ACLMessage msg1 = receive();
			if(msg1 != null) {
				System.out.println("Recibido algo");
			} else {
				block();
				System.out.println("Recibido 2");
			}
		}

		//se comparan las alternativas mirando para cada par, la funcionII y calculando el indice de preferencia individual
		public void matrizIndicesPreferencias() {
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				ArrayList<Float> aux = new ArrayList<Float>();
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					if(i != j) {
						ArrayList<Float> auxDiferenciasFuncion = new ArrayList<Float>();
						for(int k = 0; k < getDatosProblema().getNumCriterios(); k++) {
							if(getDatosProblema().getArrayMaxMinAtributos().get(k) == true)
							auxDiferenciasFuncion.add(funcionTipoI(
									getDatosProblema().getArrayValoresAtributos().get(i).get(k), 
									getDatosProblema().getArrayValoresAtributos().get(j).get(k)));
							else 
								auxDiferenciasFuncion.add(funcionTipoI(
										getDatosProblema().getArrayValoresAtributos().get(j).get(k),
										getDatosProblema().getArrayValoresAtributos().get(i).get(k)));
						}
						
						aux.add(indicePreferenciaIndividual(auxDiferenciasFuncion));
					} else {
						aux.add(-1f);
					}
				}
				getMatrizIndicesPreferencia().add(aux);
			}		
		}
		
		//Pseudocriterios. Funciones Tipo
		//Criterio usual
		public float funcionTipoI(float valor1, float valor2) { 
			if(valor1 > valor2) 
				return 1;
			else if(valor1 == valor2) 
				return 0;
			else 
				return -1;
		}
		
		// min 4:20 https://www.youtube.com/watch?v=mNwJYvn9ZsQ
		//Preferencia lineal
		public float funcionTipoII(float valor1, float valor2) { 						
			if(valor1 > valor2) 
				return 1;
			else 
				return (valor1/ valor2);
		}

		public float indicePreferenciaIndividual(ArrayList<Float> pesos) {//Multiplica pesos por preferencia
			float suma = 0;
			for(int i = 0; i < pesos.size(); i++) {
				if(pesos.get(i) > -1)
					suma += pesos.get(i) * getImportanciaRelativa().getImportancias().get(i);		
			}
			return suma;
		}
		
		public void calculosFlujosPositivos() {
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				System.out.println("Alternativa: " + i + " con flujo positivo: " + flujoPositivoAlternativa(i));
			}
			System.out.println();
		}
		
		public float flujoPositivoAlternativa(int alternativa) {
			float aux = 0;
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				if(i != alternativa)
					aux += getMatrizIndicesPreferencia().get(alternativa).get(i);
			}
			//System.out.println("Aux vale " + aux + " " + getDatosProblema().getNumCriterios());
			
			float a = (getDatosProblema().getNumAlternativas() - 1);
			a = 1 / a;
			a *= aux;
			return a;
		}
		
		public void calculosFlujosNegativos() {
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				System.out.println("Alternativa: " + i + " con flujo negativo: " + flujoNegativoAlternativa(i));
			}
			System.out.println();
		}
		
		//Cambiamos i por j
		public float flujoNegativoAlternativa(int alternativa) {
			float aux = 0;
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				if(i != alternativa) {
					aux += getMatrizIndicesPreferencia().get(i).get(alternativa);
					//System.out.println("Sumando el elemento " + getMatrizIndicesPreferencia().get(i).get(alternativa) + " i j" + i + " " + alternativa);
				}
			}
			
			float a = (getDatosProblema().getNumAlternativas() - 1);
			a = 1 / a;
			a *= aux;
			return a;
		}
		
		public void calculosFlujoNeto() {
			prioridadesFinal = new ArrayList<Float>();
			
			System.out.println("Promehee " + getImportanciaRelativa().getNombre());
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				float aux = flujoPositivoAlternativa(i) - flujoNegativoAlternativa(i);
					System.out.println("Alternativa: " + i + " con flujo neto: " + aux);
					getPrioridadesFinal().add(aux);
			}
			System.out.println();
		}
		
		public lectorProblema getDatosProblema() {
			return datosProblema;
		}

		public importanciaRelativaIndividual getImportanciaRelativa() {
			return importanciaRelativa;
		}
		
		public ArrayList<ArrayList<Float>> getMatrizIndicesPreferencia() {
			return matrizIndicesPreferencia;
		}
		
		public ArrayList<Float> getPrioridadesFinal() {
			return prioridadesFinal;
		}
		
		public void showMatrizIndicesPreferencia() {
			System.out.println("Matriz Indices de preferencia");
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					System.out.printf("%.2f  ", getMatrizIndicesPreferencia().get(i).get(j));
				}
				System.out.println();
			}
		}
	}
	
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}
} 

