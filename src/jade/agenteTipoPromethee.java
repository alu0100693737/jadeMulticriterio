package jade;
import java.util.ArrayList;
import java.util.Collections;

import jade.agenteTipoAHP.comportamientoAHP;
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
	
	public final float precision = 0.20f; //Precision acercamiento consenso, cuanto más bajo mayor consenso

	protected void setup() { 
		//System.out.println("Creando el agente");
		//System.out.println("\nHola! El agente "+ getAID().getName()+" está listo.\n");

		Object[] args = getArguments();         // Obtiene los argumentos dados en la inicialización del comprador
		if (args != null && args.length == 2) {  // Tiene que haber al menos un argumento
			if((args[0] instanceof lectorProblema) && (args[1] instanceof importanciaRelativaIndividual)) {	

				datosProblema = new lectorProblema((lectorProblema) args[0]);
				importanciaRelativa = new importanciaRelativaIndividual((importanciaRelativaIndividual) args[1]);

				addBehaviour(new comportamientoPromethee());

				//Replica
				addBehaviour(new CyclicBehaviour() {

					@Override
					public void action() {
						// TODO Auto-generated method stub


						ACLMessage msg1 = receive();
						if(msg1 != null) {
							System.out.println("\nRecibida negociacion " + msg1.getContent());
							String aux = msg1.getContent().substring(1, msg1.getContent().length() - 2);

							String aux2[] = aux.split(",\\s+");

							ArrayList<Float> aux3 = new ArrayList<Float>();
							for(int i = 0; i < aux2.length; i++) {
								aux3.add(Float.parseFloat(aux2[i]));
							}

							System.out.println("Antes Nombre " + getImportanciaRelativa().getNombre() + " " + getImportanciaRelativa().getImportancias());

							modificarImportanciasRelativas(aux3);
							System.out.println("Despues Nombre " + getImportanciaRelativa().getNombre() + " " + getImportanciaRelativa().getImportancias());

								matrizIndicesPreferencia = new ArrayList<ArrayList<Float>>();
								matrizIndicesPreferencias();

								calculosFlujoNeto(); //Se calcula el positivo, el negativo y el neto
								 
								//showPrioridadesFinales();
								
								ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
								msg.setContent(getName() + "\n" + getImportanciaRelativa().getNombre() + "\n" + getPrioridadesFinal());
								msg.addReceiver(new AID("agenteModerador", AID.ISLOCALNAME));
								send(msg);

							} else {
								block();

								//addBehaviour(new comportamientoPromethee());
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
	class comportamientoPromethee extends OneShotBehaviour {

		@Override
		public void action() {
			//System.out.println("Comportamiento Promethee");
			matrizIndicesPreferencia = new ArrayList<ArrayList<Float>>();
			matrizIndicesPreferencias();

			//System.out.println("Matriz Indices Preferencias");
			//showMatrizIndicesPreferencia();

			//calculosFlujosPositivos();

			//calculosFlujosNegativos();

			calculosFlujoNeto(); //Se calcula el positivo, el negativo y el neto

			ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
			msg.setContent(getName() + "\n" + getImportanciaRelativa().getNombre() + "\n" + getPrioridadesFinal());
			msg.addReceiver(new AID("agenteModerador", AID.ISLOCALNAME));
			send(msg);
		}
	}

	public lectorProblema getDatosProblema() {
		return datosProblema;
	}

	public importanciaRelativaIndividual getImportanciaRelativa() {
		return importanciaRelativa;
	}
	
	public void modificarImportanciasRelativas(ArrayList<Float> importanciasGrupo) {
	
		for(int i = 0; i < getDatosProblema().getNumCriterios(); i++) {
			//System.out.println("Num " + i + " " + getImportanciaRelativa().getNombre());
			//0.5 a 0.1 = 0.4 descendemos y sumamos en el resto si se puede de forma equilibrada
			//System.out.println("Mayores");
			//Comprobamos que se puede modificar ese valor sin suponer un cambio de prioridad
			while(getImportanciaRelativa().getImportancias().get(i) - importanciasGrupo.get(i) >= (2 * precision)) {
				//System.out.println(getImportanciaRelativa().getImportancias().get(i) + "  - " +  importanciasGrupo.get(i));
				//Comprobamos si podemos modificar los demas valores
				//System.out.println("Mayores");
				float aux = precision / (getDatosProblema().getNumCriterios() - 1);
				//System.out.println("Aux vale " + aux);
				boolean sepuede = true; 

				for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
					if(i != j) {
						if(Math.abs(getImportanciaRelativa().getImportancias().get(i) - importanciasGrupo.get(i)) <= (2 * aux)) {

							//System.out.println("Fallo en " + getImportanciaRelativa().getImportancias().get(i) + " - " + importanciasGrupo.get(i));
							sepuede = false;

						}
					}
				}

				if(sepuede == true) {
					//Creo y asigno 0
					ArrayList<Float> nueva = new ArrayList<Float>();
					for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
						nueva.add(0.0f);
					}

					//Asigno los futuros valores
					for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
						if(i != j) {

							//System.out.println("ponemos en " + j + " valor " + getImportanciaRelativa().getImportancias().get(j) + aux);
							nueva.set(j, getImportanciaRelativa().getImportancias().get(j) + aux);
						}
					}
					nueva.set(i, getImportanciaRelativa().getImportancias().get(i) - precision);
					//System.out.println("***************************************************\n");
					//System.out.println("Actuales " + getImportanciaRelativa().getImportancias());
					//System.out.println("nueva " +  nueva);
					
					//Compruebo que no han cambiado las prioridades de cada uno de los criterios
					for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
						if( getPosMAx(getImportanciaRelativa().getImportancias(), j) != getPosMAx(nueva, j)) {
							sepuede = false;
						}
						//System.out.println("Max de j  " + j + " " + getPosMAx(getImportanciaRelativa().getImportancias(), j) + " " + getPosMAx(nueva, j));
					}

					//si cumple las especificaciones
					if(sepuede) {
						//System.out.println("Se puede");
						for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
							if(i != j) {
								//System.out.println("ponemos en " + j + " valor " + getImportanciaRelativa().getImportancias().get(j) + aux);
								getImportanciaRelativa().getImportancias().set(j, getImportanciaRelativa().getImportancias().get(j) + aux);
							}
						}
						getImportanciaRelativa().getImportancias().set(i, getImportanciaRelativa().getImportancias().get(i) - precision);
						//getImportanciaRelativa().showImportancias();
					} else {
						break;
					}
				}
			}

			//0.1 a 0.4 = -0.4 descendemos y sumamos en el resto si se puede de forma equilibrada
			//System.out.println("Menores");

			//Comprobamos que se puede modificar ese valor sin suponer un cambio de prioridad
			while(getImportanciaRelativa().getImportancias().get(i) - importanciasGrupo.get(i) <= -(2 * precision)) {
				//System.out.println("Menores");
				//System.out.println(getImportanciaRelativa().getImportancias().get(i) + "  - " +  importanciasGrupo.get(i));
				//Comprobamos si podemos modificar los demas valores
				float aux = precision / (getDatosProblema().getNumCriterios() - 1);
				//System.out.println("Aux vale " + aux);
				boolean sepuede = true; 
				for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
					if(i != j) {
						if(Math.abs(getImportanciaRelativa().getImportancias().get(i) - importanciasGrupo.get(i)) <= (2 * aux)) {

							//System.out.println("Fallo en " + getImportanciaRelativa().getImportancias().get(i) + " - " + importanciasGrupo.get(i));
							sepuede = false;

						}
					}
				}

				if(sepuede == true) {
					//Creo y asigno 0
					ArrayList<Float> nueva = new ArrayList<Float>();
					for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
						nueva.add(0.0f);
					}

					//Asigno los futuros valores
					for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
						if(i != j) {

							//System.out.println("ponemos en " + j + " valor " + getImportanciaRelativa().getImportancias().get(j) + aux);
							nueva.set(j, getImportanciaRelativa().getImportancias().get(j) - aux);
						}
					}
					nueva.set(i, getImportanciaRelativa().getImportancias().get(i) + precision);
					
					//System.out.println("Actuales " + getImportanciaRelativa().getImportancias());
					//System.out.println("nueva " +  nueva);
					
					//Compruebo que no han cambiado las prioridades de cada uno de los criterios
					for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
						if( getPosMAx(getImportanciaRelativa().getImportancias(), j) != getPosMAx(nueva, j)) {
							sepuede = false;
							System.out.println("NO SE PUEDE; PROHIBIDOOO" + getImportanciaRelativa().getNombre());
							break;
						}
						//System.out.println("Max de j  " + j + " " + getPosMAx(getImportanciaRelativa().getImportancias(), j) + " " + getPosMAx(nueva, j));
					}

					if(sepuede) {
						//System.out.println("Se puede");
						for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
							if(i != j) {
								//System.out.println("ponemos en " + j + " valor " + getImportanciaRelativa().getImportancias().get(j) + " menos "+ aux);
								getImportanciaRelativa().getImportancias().set(j, getImportanciaRelativa().getImportancias().get(j) - aux);
							}
						}
						getImportanciaRelativa().getImportancias().set(i, getImportanciaRelativa().getImportancias().get(i) + precision);
						//getImportanciaRelativa().showImportancias();
					} else {
						break;
					}
				}
			}
		}

	}
	
	/**
	 * Devuelve el valor que ocupa en un array de mayor a menor
	 */
	public int getPosMAx(ArrayList<Float> arrayoriginal, int pos) {
		ArrayList<Float>array = new ArrayList<Float>(arrayoriginal);
		//System.out.println("Elemento " + pos + " de " + arrayoriginal);
		//System.out.println("Pos " + pos + " deberia " + Collections.max(array) + " " + array.indexOf(Collections.max(array)));
		int valor = 0;
		for(int i = 0; i < getImportanciaRelativa().getImportancias().size(); i++) {
			if(pos == array.indexOf(Collections.max(array))) {
				//System.out.println("Encontrado mayor en Pos ranking " + valor + " deberia " + Collections.max(array) + " pos array" + array.indexOf(Collections.max(array)));

				return valor;
			} else {
				valor += 1;
				array.remove(array.indexOf(Collections.max(array)));
				//System.out.println("PRueba " + array);
				//System.out.println("HEmos eliminado el mayor");
				//System.out.println("array " + array);
				//System.out.println("***********************************\n");
			}
		}
		return valor;
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

		//System.out.println("Promehee " + getImportanciaRelativa().getNombre());
		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
			float aux = flujoPositivoAlternativa(i) - flujoNegativoAlternativa(i);
			//System.out.println("Alternativa: " + i + " con flujo neto: " + aux);
			getPrioridadesFinal().add(aux);
		}
		//System.out.println();
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
	public void showPrioridadesFinales() {
		System.out.println("\n***********************************************");
		System.out.println("Promethee: " + getImportanciaRelativa().getNombre());
		System.out.println("Prioridades finales");
		for(int i = 0; i < getPrioridadesFinal().size(); i++) {
			System.out.println("Alternativa " + i + " con prioridad " + getPrioridadesFinal().get(i));
		}
		System.out.println("****************************************************\n");
	}

	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}
} 

