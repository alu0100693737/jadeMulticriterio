package jade;
import java.util.ArrayList;

import jade.agenteTipoElectre.comportamientoElectre;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lecturaFicheros.importanciaRelativaIndividual;
import lecturaFicheros.lectorProblema;

public class agenteTipoAHP extends Agent {

	private lectorProblema datosProblema;
	private importanciaRelativaIndividual importanciaRelativa;

	//Prioridades
	private ArrayList<ArrayList<Float>> matrizComparacionPares;
	private ArrayList<ArrayList<Float>> matrizComparacionParesNormalizada;
	private ArrayList<Float> conjuntoSumaPorCriterio;
	private ArrayList<Float> conjuntoPrioridades;

	//Alternativas
	private ArrayList<ArrayList<ArrayList<Float>>> conjuntoMatricesAlternativas;
	private ArrayList<ArrayList<ArrayList<Float>>> conjuntoMatricesAlternativasNormalizada;
	private ArrayList<ArrayList<Float>> conjuntoMatrizPrioridadesAlternativas;
	
	private ArrayList<Float> prioridadesFinal;
	
	protected void setup() { 
		//System.out.println("Creando el agente");
		//System.out.println("\nHola! El agente "+ getAID().getName()+" está listo.\n");

		Object[] args = getArguments();         // Obtiene los argumentos dados en la inicialización del comprador
		if (args != null && args.length == 2) {  // Tiene que haber al menos un argumento
			if((args[0] instanceof lectorProblema) && (args[1] instanceof importanciaRelativaIndividual)) {	

				datosProblema = new lectorProblema((lectorProblema) args[0]);
				importanciaRelativa = new importanciaRelativaIndividual((importanciaRelativaIndividual) args[1]);
				//System.out.println("\n------------------------------------------------------------------\n");

				addBehaviour(new comportamientoAHP());
				
				//Replica
				addBehaviour(new CyclicBehaviour() {
					
					@Override
					public void action() {
						// TODO Auto-generated method stub
						block();
						
						ACLMessage msg1 = receive();
						if(msg1 != null) {
							System.out.println("Recibida negociacion " + msg1.getContent());
							String aux[] = msg1.getContent().split("\\s+");
							System.out.println("size " + aux.length + " " + aux[0]);
						
							//Cambiamos importancias relativas lo posible entre las dos alternativas
							float diferencia = Math.abs(getImportanciaRelativa().getImportancias().get((int)Float.parseFloat(aux[0])))
									- getImportanciaRelativa().getImportancias().get((int)Float.parseFloat(aux[1]));
							
							System.out.println("Diferencia: " + diferencia);
							System.out.println("Antes");
							getImportanciaRelativa().showImportancias();
							//Si se puede realizar una modificacion sin cambiar mis preferencias personales
							while(diferencia > 0.2) {
								getImportanciaRelativa().getImportancias().set(
										Integer.getInteger(aux[0]), (float) (getImportanciaRelativa().getImportancias().get(Integer.getInteger(aux[0])) - 0.1));
								getImportanciaRelativa().getImportancias().set(
										Integer.getInteger(aux[1]), (float) (getImportanciaRelativa().getImportancias().get(Integer.getInteger(aux[1])) + 0.1));
							}
							System.out.println("Despues");
							getImportanciaRelativa().showImportancias();
							
						
							
							
						} else {
							block();
							
							//addBehaviour(new comportamientoAHP());
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
	class comportamientoAHP extends OneShotBehaviour {

		@Override
		public void action() {
			//System.out.println("Comportamiento Proceso Analitico Jerarquico");
			//System.out.println("Se calcula la matriz de comparacion por pares");
			calcularMatrizComparacionPares();

			//showMatrizComparacionPares();

			//System.out.println("Se calcula la matriz normalizada");
			calcularMatrizComparacionesParesNormalizada();

			//showMatrizComparacionParesNormalizada();

			calcularPrioridades();
			//showConjuntoPrioridades();

			calcularMatricesAlternativasSegunPrioridades();
			
			//showMatricesAlternativasSegunPrioridades();
			
			calcularMatricesAlternativasSegunPrioridadesNormalizada();
			
			//showMatricesAlternativasSegunPrioridadesNormalizada();
			
			calcularConjuntoMatrizPrioridadesAlternativas();
			
			//showConjuntoMatrizPrioridadAlternativas();
			
			calcularPrioridadFinal();
			
			//showPrioridadesFinal();
			
			ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
			msg.setContent(getName() + "\n" + getPrioridadesFinal());
			msg.addReceiver(new AID("agenteModerador", AID.ISLOCALNAME));
			send(msg);
		}

		/**
		 * Metodo que compara las preferencias personales, si son iguales en dos criterios -> 1, si son mayores -> 3 + 1/criterios * 4
		 * ej : 1/criterios = 0.2; diferencia entre dos criterios: 0.3 / 0.2 = 1.5 -> teniendo 3 + 0.5 * 4 = 5.
		 */
		public void calcularMatrizComparacionPares() {
			matrizComparacionPares = new ArrayList<ArrayList<Float>>();
			float valorAumento = 1.0f / getDatosProblema().getNumCriterios();
			//System.out.println("Valor aumento vale " + valorAumento + " criterios " + getDatosProblema().getNumCriterios());
			for(int i = 0; i < getDatosProblema().getNumCriterios(); i++) {
				ArrayList<Float> preferencias = new ArrayList<Float>();
				for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
					if(i != j) {
						float diferencia = getImportanciaRelativa().getImportancias().get(i) - getImportanciaRelativa().getImportancias().get(j);
						if(diferencia > 0) {
							//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
							if(diferencia < valorAumento) { 
								preferencias.add(3.0f);
							}
							else {
								//System.out.println("Diferencia " + diferencia + " valoraumento " + valorAumento);
								float valor = diferencia / valorAumento;
								//System.out.println("Valor vale " + valor);
								valor -= 1.0f;
								valor = valor * 4; //valor de aumento
								valor += 3.0f;
								preferencias.add((float) Math.ceil(valor));
								//System.out.println("Añadido el valor " + valor);
							}
						} else if (diferencia == 0.0f) { //igual
							//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
							preferencias.add(1.0f);

						} else { //dividido
							//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
							if(diferencia > (-valorAumento)) {
								//System.out.println("Dividiendo por poco");

								float aux = 1.0f / 3.0f;
								preferencias.add(aux);
								//System.out.println("Añadido un " + aux);
							}
							else {
								//System.out.println("Dividiendo por mucho");
								float valor = diferencia / -valorAumento;
								//System.out.println("Valor " + valor);
								valor -= 1.0f;
								//System.out.println("Valor " + valor);
								valor = valor * 4; //Factor de aumento
								//System.out.println("Valor " + valor);
								valor += 3.0f;
								//System.out.println("Valor " + valor);
								valor = 1.0f / valor;

								preferencias.add(valor);
								//System.out.println("Añadido el valor " + valor);
							}
						}
					} else {
						preferencias.add(1.0f);
						//System.out.println("Iguales, añadido un 1");
					}
				} 
				getMatrizComparacionPares().add(preferencias);
			}
		}

		public void calcularMatrizComparacionesParesNormalizada() {
			matrizComparacionParesNormalizada = new ArrayList<ArrayList<Float>>();
			conjuntoSumaPorCriterio = new ArrayList<Float>();
			conjuntoPrioridades = new ArrayList<Float>();

			for(int i = 0; i < getDatosProblema().getNumCriterios(); i++) {
				float suma = 0.0f;
				for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
					suma += getMatrizComparacionPares().get(j).get(i);
				}
				getConjuntoSumaPorCriterio().add(suma);
			}

			//showConjuntoSumaPorCriterio();

			//Calculando matriz normalizada
			for(int i = 0; i < getDatosProblema().getNumCriterios(); i++) {
				ArrayList<Float> criterioFila = new ArrayList<Float>();
				for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
					criterioFila.add(getMatrizComparacionPares().get(i).get(j) / getConjuntoSumaPorCriterio().get(j));
				}
				getMatrizComparacionParesNormalizada().add(criterioFila);
			}
		}
		
		public void calcularPrioridades() {
			//Prioridades
			for(int i = 0; i < getDatosProblema().getNumCriterios(); i++) {
				float suma = 0;
				for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
					suma += getMatrizComparacionParesNormalizada().get(i).get(j);
				}
				suma /= getDatosProblema().getNumCriterios();
				getConjuntoPrioridades().add(suma);
			}
		}
		
		//Conjunto de matrices por prioridad, falta normalizar y sumar prioridad
		public void calcularMatricesAlternativasSegunPrioridades() {
			conjuntoMatricesAlternativas = new ArrayList<ArrayList<ArrayList<Float>>>();
			
			for(int i = 0; i < getDatosProblema().getNumCriterios(); i++) 
				getConjuntoMatricesAlternativas().add(calcularMatrizAlternativasSegunPrioridadIndividual(i));
		}

		public ArrayList<ArrayList<Float>> calcularMatrizAlternativasSegunPrioridadIndividual(int criterio) {
			
			ArrayList<ArrayList<Float>> matrizPrioridadesCriterio = new ArrayList<ArrayList<Float>>();

			//Calculamos los rangos de valor entre cada una de las alternativas
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;

			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				if(getDatosProblema().getArrayValoresAtributos().get(i).get(criterio) < min) {
					min = getDatosProblema().getArrayValoresAtributos().get(i).get(criterio);
				} 

				if(getDatosProblema().getArrayValoresAtributos().get(i).get(criterio) > max) {
					max = getDatosProblema().getArrayValoresAtributos().get(i).get(criterio);
				} 
			}
			//System.out.println("Max vale " + max + " min " + min);
			
			float rango = max - min;
			//System.out.println("Rango de " + rango);
			float medida = rango / getDatosProblema().getNumAlternativas();
			//System.out.println("Medida " + medida);
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				ArrayList<Float> filaComparacion = new ArrayList<Float>();
				
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					if(i == j) {
						filaComparacion.add(1.0f);
					} else {
						float diferencia = getDatosProblema().getArrayValoresAtributos().get(i).get(criterio) - getDatosProblema().getArrayValoresAtributos().get(j).get(criterio);
						
						//Se consideran elementos min y maximos!!! IMPORTANTE
						if(getDatosProblema().getArrayMaxMinAtributos().get(criterio) == true) {
							if(diferencia > 0) {
								//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
								if(diferencia < medida) { 
									filaComparacion.add(3.0f);
								}
								else {
									//System.out.println("Diferencia " + diferencia + " valoraumento " + medida);
									float valor = diferencia / medida;
									//System.out.println("Valor vale " + valor);
									valor -= 1.0f;
									//System.out.println("Valor vale " + valor);
									valor = valor * 2; //valor de aumento
									//System.out.println("Valor vale " + valor);
									valor += 3.0f;
									//System.out.println("Valor vale " + valor);
									filaComparacion.add((float) Math.ceil(valor));
									//System.out.println("Añadido el valor " + valor);
								}
							} else if (diferencia == 0.0f) { //igual
								//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
								filaComparacion.add(1.0f);
	
							} else { //dividido
								//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
								if(diferencia > (-medida)) {
									//System.out.println("Dividiendo por poco");
	
									float aux = 1.0f / 3.0f;
									filaComparacion.add(aux);
									//System.out.println("Añadido un " + aux);
								}
								else {
									//System.out.println("Dividiendo por mucho");
									float valor = diferencia / - medida;
									//System.out.println("Valor " + valor);
									valor -= 1.0f;
									//System.out.println("Valor " + valor);
									valor = valor * 2; //Factor de aumento
									//System.out.println("Valor " + valor);
									valor += 3.0f;
									//System.out.println("Valor " + valor);
									valor = 1.0f / valor;
	
									filaComparacion.add(valor);
									//System.out.println("Añadido el valor " + valor);
								}
							}
						} //Criterio max 
						else {
							
							if(diferencia < 0) {
								//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
								if(diferencia > -medida) { 
									filaComparacion.add(3.0f);
								}
								else {
									//System.out.println("Diferencia " + diferencia + " valoraumento " + medida);
									float valor = diferencia / -medida;
									//System.out.println("Valor vale " + valor);
									valor -= 1.0f;
									//System.out.println("Valor vale " + valor);
									valor = valor * 2; //valor de aumento
									//System.out.println("Valor vale " + valor);
									valor += 3.0f;
									valor = (float) Math.ceil(valor);
									//System.out.println("Valor vale " + valor);
									filaComparacion.add(valor);
									//System.out.println("Añadido el valor " + valor);
								}
							} else if (diferencia == 0.0f) { //igual
								//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
								filaComparacion.add(1.0f);
	
							} else { //dividido
								//System.out.println("Diferencia del tipo  " + diferencia + " entre preferencia " + i + " " + j);
								if(diferencia < medida) {
									//System.out.println("Dividiendo por poco");
	
									float aux = 1.0f / 3.0f;
									filaComparacion.add(aux);
									//System.out.println("Añadido un " + aux);
								}
								else {
									//System.out.println("Dividiendo por mucho");
									float valor = diferencia / medida;
									//System.out.println("Valor " + valor);
									valor -= 1.0f;
									//System.out.println("Valor " + valor);
									valor = valor * 2; //Factor de aumento
									//System.out.println("Valor " + valor);
									valor += 3.0f;
									//System.out.println("Valor " + valor);
									valor = 1.0f / valor;
	
									filaComparacion.add(valor);
									//System.out.println("Añadido el valor " + valor);
								}
							}
						}
					}
				}
				matrizPrioridadesCriterio.add(filaComparacion);
			}
			return matrizPrioridadesCriterio;
		}


		public void calcularMatricesAlternativasSegunPrioridadesNormalizada() {
			conjuntoMatricesAlternativasNormalizada = new ArrayList<ArrayList<ArrayList<Float>>>();
			
			for(int i = 0; i < getConjuntoMatricesAlternativas().size(); i++) {
				//System.out.println("\nAlternativa num " + i);
				ArrayList<Float> sumasColumnas = new ArrayList<Float>();
				
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					float suma = 0;
					for(int k = 0; k < getDatosProblema().getNumAlternativas(); k++) {
						suma += getConjuntoMatricesAlternativas().get(i).get(k).get(j);
					}
					sumasColumnas.add(suma);
					//System.out.println("Valor suma " + suma);
				}
				
				ArrayList<ArrayList<Float>> matrizAlternativaNormalizada = new ArrayList<ArrayList<Float>>();
				
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					ArrayList<Float> filaNormalizada = new ArrayList<Float>();
					
					for(int k = 0; k < getDatosProblema().getNumAlternativas(); k++) {
						//System.out.println("Dividiendo " + getConjuntoMatricesAlternativas().get(i).get(j).get(k) + " " + sumasColumnas.get(k));
						filaNormalizada.add(getConjuntoMatricesAlternativas().get(i).get(j).get(k) / sumasColumnas.get(k));
					}
					matrizAlternativaNormalizada.add(filaNormalizada);
				}
				getConjuntoMatricesAlternativasNormalizada().add(matrizAlternativaNormalizada);
			}
		}
		
		public void calcularConjuntoMatrizPrioridadesAlternativas() {
			conjuntoMatrizPrioridadesAlternativas = new ArrayList<ArrayList<Float>>();
			//calculamos los vectores de cada matriz, sumando cada fila
			for(int i = 0; i < getConjuntoMatricesAlternativasNormalizada().size(); i++) {
				
				ArrayList<Float> aux = new ArrayList<Float>();
				
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					float suma = 0;
					for(int k = 0; k < getDatosProblema().getNumAlternativas(); k++) {
						//System.out.println("Sumando " + getConjuntoMatricesAlternativasNormalizada().get(i).get(j).get(k));
						suma += getConjuntoMatricesAlternativasNormalizada().get(i).get(j).get(k);
					}
					suma /= getDatosProblema().getNumAlternativas();
					aux.add(suma);
				}
				getConjuntoMatrizPrioridadesAlternativas().add(aux);
			}
		}
		
		public void calcularPrioridadFinal() {
			prioridadesFinal = new ArrayList<Float>();
			for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
				//System.out.println("Alternativa  " + i);;
				float suma = 0;
				for(int j = 0; j < getConjuntoMatrizPrioridadesAlternativas().size(); j++) { //mismo valor que num criterios
					suma += getConjuntoPrioridades().get(j) * getConjuntoMatrizPrioridadesAlternativas().get(j).get(i);
				}
				getPrioridadesFinal().add(suma);
			}
		}
		
		public void showConjuntoMatrizPrioridadAlternativas() {
			for(int i = 0; i < getConjuntoMatrizPrioridadesAlternativas().size(); i++) {
				System.out.println("Prioridad de alternativa " + i);
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					System.out.println(getConjuntoMatrizPrioridadesAlternativas().get(i).get(j));
				}
			}	
		}
		
		public void showMatrizComparacionPares() {
			System.out.println("\nMatriz de comparación por pares: \n");
			for(int i = 0; i < getMatrizComparacionPares().size(); i++) {
				for(int j = 0; j < getMatrizComparacionPares().size(); j++) {
					System.out.printf("%.2f  ", getMatrizComparacionPares().get(i).get(j));
				}
				System.out.println();
			}
		}
		
		public void showMatrizComparacionParesNormalizada() {
			System.out.println("Matriz de comparación por pares: \n");
			for(int i = 0; i < getMatrizComparacionParesNormalizada().size(); i++) {
				for(int j = 0; j < getMatrizComparacionParesNormalizada().size(); j++) {
					System.out.printf("%.2f  ", getMatrizComparacionParesNormalizada().get(i).get(j));
				}
				System.out.println();
			}
		}

		public void showConjuntoSumaPorCriterio() {
			System.out.println("Conjunto suma por criterio");
			for(int i = 0; i < getConjuntoSumaPorCriterio().size(); i++) 
				System.out.print(getConjuntoSumaPorCriterio().get(i) + " ");
		}

		public void showConjuntoPrioridades() {
			System.out.println("Conjunto de Prioridades");
			for(int i = 0; i < getConjuntoPrioridades().size(); i++)
				System.out.println(getConjuntoPrioridades().get(i) + " ");
		}
		
		public void showMatriz(ArrayList<ArrayList<Float>> datos) {
			for(int i = 0; i < datos.size(); i++) {
				for(int j = 0; j < datos.size(); j++) {
					System.out.printf("%.2f  ", datos.get(i).get(j));
				}
				System.out.println();
			}
		}
		
		public void showMatricesAlternativasSegunPrioridades() {
			for(int i = 0; i < getConjuntoMatricesAlternativas().size(); i++) {
				System.out.println("\nAlternativa num " + i);
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					for(int k = 0; k < getDatosProblema().getNumAlternativas(); k++) {
						System.out.printf("%.2f  ", getConjuntoMatricesAlternativas().get(i).get(j).get(k));
					}
					System.out.println();
				}
				System.out.println();
			}
		}
		
		public void showMatricesAlternativasSegunPrioridadesNormalizada() {
			for(int i = 0; i < getConjuntoMatricesAlternativasNormalizada().size(); i++) {
				System.out.println("\nAlternativa num " + i);
				for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
					for(int k = 0; k < getDatosProblema().getNumAlternativas(); k++) {
						System.out.printf("%.2f  ", getConjuntoMatricesAlternativasNormalizada().get(i).get(j).get(k));
					}
					System.out.println();
				}
				System.out.println();
			}
		}
		
		public void showPrioridadesFinal() {
			System.out.println("AHP " + getImportanciaRelativa().getNombre());
			for(int i = 0; i < getPrioridadesFinal().size(); i++) {
				System.out.println("Prioridad num " + i + " con: " + getPrioridadesFinal().get(i));
			}
		}
		
		public ArrayList<Float> getConjuntoSumaPorCriterio() {
			return conjuntoSumaPorCriterio;
		}
	
		public ArrayList<Float> getConjuntoPrioridades() {
			return conjuntoPrioridades;
		}

		public ArrayList<ArrayList<ArrayList<Float>>> getConjuntoMatricesAlternativas() {
			return conjuntoMatricesAlternativas;
		}
		
		public ArrayList<ArrayList<ArrayList<Float>>> getConjuntoMatricesAlternativasNormalizada() {
			return conjuntoMatricesAlternativasNormalizada;
		}
		
		public ArrayList<ArrayList<Float>> getConjuntoMatrizPrioridadesAlternativas() {
			return conjuntoMatrizPrioridadesAlternativas;
		}
		
		public ArrayList<Float> getPrioridadesFinal() {
			return prioridadesFinal;
		}
		
	}

	public lectorProblema getDatosProblema() {
		return datosProblema;
	}

	public importanciaRelativaIndividual getImportanciaRelativa() {
		return importanciaRelativa;
	}

	public ArrayList<ArrayList<Float>> getMatrizComparacionPares() {
		return matrizComparacionPares;
	}

	public ArrayList<ArrayList<Float>> getMatrizComparacionParesNormalizada() {
		return matrizComparacionParesNormalizada;
	}

	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}
}
