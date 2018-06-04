package jade;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.messaging.MatchAllFilter;
import jade.domain.persistence.ReloadAgent;
import jade.lang.acl.ACLMessage;
import jade.tools.logging.ontology.GetAllLoggers;
import lecturaFicheros.lectorProblema;
import lecturaFicheros.importanciaRelativaIndividual;

public class agenteTipoElectre extends Agent {

	private lectorProblema datosProblema;
	private importanciaRelativaIndividual importanciaRelativa;

	private ArrayList<ArrayList<Float>> solucionElectre;

	private ArrayList<ArrayList<Integer>> conjuntoConcordancia;
	private ArrayList<ArrayList<Integer>> conjuntoDiscordancia;

	private ArrayList<ArrayList<Float>> matrizConcordancia;
	private ArrayList<ArrayList<Float>> matrizDiscordancia;

	private ArrayList<ArrayList<Integer>> matrizDominanciaConcordancia;
	private ArrayList<ArrayList<Integer>> matrizDominanciaDiscordancia;

	private ArrayList<ArrayList<Integer>> matrizDominanciaAgregada;

	private ArrayList<Float> prioridadesFinales;

	private final float umbralConcordancia = 0.55f;
	private final float umbralDiscordancia = 0.71f;

	private float[] vector; 
	private ArrayList<Point> permutaciones;
	
	public final float precision = 0.20f; //Precision acercamiento consenso, cuanto más bajo mayor consenso

	@Override
	protected void setup() { 

		//System.out.println("\nHola! El agente "+getAID().getName()+" está listo.\n");

		Object[] args = getArguments();         // Obtiene los argumentos dados en la inicialización del comprador
		if (args != null && args.length == 2) {  // Tiene que haber al menos un argumento
			if((args[0] instanceof lectorProblema) && (args[1] instanceof importanciaRelativaIndividual)) {	

				datosProblema = new lectorProblema((lectorProblema) args[0]);
				importanciaRelativa = new importanciaRelativaIndividual((importanciaRelativaIndividual) args[1]);

				addBehaviour(new comportamientoElectre());

				//Replica
				addBehaviour(new CyclicBehaviour() {

					@Override
					public void action() {
						// TODO Auto-generated method stub
						block();

						ACLMessage msg1 = receive();
						if(msg1 != null) {
							System.out.println("Recibida negociacion " + msg1.getContent());
							String aux = msg1.getContent().substring(1, msg1.getContent().length() - 2);

							String aux2[] = aux.split(",\\s+");

							ArrayList<Float> aux3 = new ArrayList<Float>();
							for(int i = 0; i < aux2.length; i++) {
								aux3.add(Float.parseFloat(aux2[i]));
							}

							System.out.println("Antes Nombre " + getImportanciaRelativa().getNombre() + " " + getImportanciaRelativa().getImportancias());

							modificarImportanciasRelativas(aux3);
							System.out.println("Despues Nombre " + getImportanciaRelativa().getNombre() + " " + getImportanciaRelativa().getImportancias());


							//conversionImportanciasRelativas()//Cambiamos importancias relativas lo posible entre las dos alternativas
							

								calcularMatrizDecisionNormalizada();
								//showSolucionElectre();
								calcularMatrizDecisionPonderada();
								//System.out.println("\nMatriz Ponderada\n");
								//showSolucionElectre();

								calcularConjuntoConcordanciaDiscordancia();
								//showConjuntosConcordanciaDiscordancia();

								calcularMatrizConcordancia();
								calcularMatrizDiscordancia();

								//showMatricesConcordanciaDiscordancia();

								calcularMatrizDominanciaConcordancia();
								calcularMatrizDominanciaDiscordancia();
								//showMatricesDominancia();

								calcularMatrizDominanciaAgregada();
								//showMatrizDominanciaAgregada();

								calcularPrioridadesFinales();

								//showPrioridadesFinales();

								ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
								//Enviamos el nombre del agente y las prioridades
								msg.setContent(getName() + "\n" + getImportanciaRelativa().getNombre() + "\n" + getPrioridadesFinales());

								msg.addReceiver(new AID("agenteModerador", AID.ISLOCALNAME));
								send(msg);

							} else {
								block();

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
	class comportamientoElectre extends OneShotBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			calcularMatrizDecisionNormalizada();
			//showSolucionElectre();
			calcularMatrizDecisionPonderada();
			//System.out.println("\nMatriz Ponderada\n");
			//showSolucionElectre();

			calcularConjuntoConcordanciaDiscordancia();
			//showConjuntosConcordanciaDiscordancia();

			calcularMatrizConcordancia();
			calcularMatrizDiscordancia();

			//showMatricesConcordanciaDiscordancia();

			calcularMatrizDominanciaConcordancia();
			calcularMatrizDominanciaDiscordancia();
			//showMatricesDominancia();

			calcularMatrizDominanciaAgregada();
			//showMatrizDominanciaAgregada();

			calcularPrioridadesFinales();

			ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
			//Enviamos el nombre del agente y las prioridades
			msg.setContent(getName() + "\n" + getImportanciaRelativa().getNombre() + "\n" + getPrioridadesFinales());
			msg.addReceiver(new AID("agenteModerador", AID.ISLOCALNAME));
			send(msg);

		}

	}

	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}

	public void modificarImportanciasRelativas(ArrayList<Float> importanciasGrupo) {
	
		//getImportanciaRelativa().showImportancias();


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

	public void calcularMatrizDecisionNormalizada() {
		solucionElectre = new ArrayList<ArrayList<Float>>();

		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
			ArrayList<Float> aux = new ArrayList<Float>();
			for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
				float suma = 0;
				for(int k = 0; k < getDatosProblema().getNumAlternativas(); k++) {
					suma += Math.pow(getDatosProblema().getArrayValoresAtributos().get(k).get(j), 2);
				}
				aux.add(getDatosProblema().getArrayValoresAtributos().get(i).get(j) / (float)(Math.sqrt(suma)));
			}
			getSolucionElectre().add(aux);
		}
	}

	public void calcularMatrizDecisionPonderada() {
		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
			for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
				setDataSolucionElectre(i, j, getSolucionElectre().get(i).get(j) * getImportanciaRelativa().getImportancias().get(j));
			}
		}
	}

	public void calcularConjuntoConcordanciaDiscordancia() {
		//Para todas las permutaciones posibles, calculamos la concordancia y discordancia
		ArrayList<Point> permutaciones = calcularPermutaciones();

		conjuntoConcordancia = new ArrayList<ArrayList<Integer>>();
		conjuntoDiscordancia = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < permutaciones.size(); i++) {
			ArrayList<Integer> auxConcordancia = new ArrayList<Integer>();
			ArrayList<Integer> auxDiscordancia = new ArrayList<Integer>();

			for(int j = 0; j < getDatosProblema().getNumCriterios(); j++) {
				//System.out.println("Comparamos : " + getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) + " " + getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j));
				if(getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) > getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j)) {
					if(getDatosProblema().getArrayMaxMinAtributos().get(j) == true) {
						//System.out.println("Concordancia");
						auxConcordancia.add(j);
					} else {
						//System.out.println("Discordancia");
						auxDiscordancia.add(j);
					}
				} else if (getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) < getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j)) {
					if(getDatosProblema().getArrayMaxMinAtributos().get(j) == true) {
						//System.out.println("Discordancia");
						auxDiscordancia.add(j);
					} else {
						//System.out.println("Concordancia");
						auxConcordancia.add(j);
					}
				} else {
					//Son valores iguales, misma concordancia y discordancia
					//System.out.println("Considerado concordancia");
					auxConcordancia.add(j);
					//auxDiscordancia.add(j + 1);
					//System.out.println("Valores iguales, MIRAR");
					//System.out.println(getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) + " " + getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j));
				}		
			}

			getConjuntoConcordancia().add(auxConcordancia);
			getConjuntoDiscordancia().add(auxDiscordancia);
		}
	}


	public void calcularMatrizConcordancia() {
		int auxPermutacion = 0;

		matrizConcordancia = new ArrayList<ArrayList<Float>>();
		//System.out.println("Empezando matriz concordancia");
		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {

			ArrayList<Float> filaConcordancia = new ArrayList<Float>();

			for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
				if(i != j) {
					//System.out.println("Elemento " + i + " " + j + " Permutacion " + auxPermutacion);
					float sumaPesos = 0.0f; 
					if((getPermutaciones().get(auxPermutacion).getX() == i) && (getPermutaciones().get(auxPermutacion).getY() == j)) {
						//System.out.println("PEPE " + conjuntoConcordancia.get(auxPermutacion).size());
						for(int k = 0; k < conjuntoConcordancia.get(auxPermutacion).size(); k++) {
							//System.out.println("Sumando " + getImportanciaRelativa().getImportancias().get(conjuntoConcordancia.get(auxPermutacion).get(k)));
							sumaPesos += getImportanciaRelativa().getImportancias().get(conjuntoConcordancia.get(auxPermutacion).get(k));

							//System.out.println("Peso acumulado " + sumaPesos);
						}
					} else {
						System.out.println("Error " + getPermutaciones().get(auxPermutacion).getX() + ", " + getPermutaciones().get(auxPermutacion).getY());
					}

					filaConcordancia.add(sumaPesos);
					auxPermutacion++;
				} else {
					filaConcordancia.add(0f);
				}

			}

			matrizConcordancia.add(filaConcordancia);
		}
	}

	public void calcularMatrizDiscordancia() {
		int auxPermutacion = 0;

		matrizDiscordancia = new ArrayList<ArrayList<Float>>();
		//System.out.println("Empezando matriz discordancia");
		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {

			ArrayList<Float> filaDiscordancia = new ArrayList<Float>();

			for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
				if(i != j) {
					//System.out.println("Posicion " + i + "," + j);
					if(getPermutaciones().get(auxPermutacion).getX() == i && getPermutaciones().get(auxPermutacion).getY() == j) {
						//System.out.println("Para i j " + i + " , " + j + getConjuntoDiscordancia().get(auxPermutacion));

						float auxNumerador = 0; 
						for(int k = 0; k < getConjuntoDiscordancia().get(auxPermutacion).size(); k++) {
							//System.out.println("estoy en " + getConjuntoDiscordancia().get(auxPermutacion).get(k));
							float aux1 = Math.abs(
									getSolucionElectre().get(i).get(getConjuntoDiscordancia().get(auxPermutacion).get(k)) - 
									getSolucionElectre().get(j).get(getConjuntoDiscordancia().get(auxPermutacion).get(k)));
							if(auxNumerador < aux1) {
								//System.out.println("Elegido numerador  discordancia en " + i + " " + j + " " + getConjuntoDiscordancia().get(auxPermutacion).get(k) + " con " + aux1);
								//System.out.println("Se ha comparado " + getSolucionElectre().get(i).get(getConjuntoDiscordancia().get(auxPermutacion).get(k)) + 
								//		" y " + 									getSolucionElectre().get(j).get(getConjuntoDiscordancia().get(auxPermutacion).get(k)));
								auxNumerador = aux1;
							}
						}
						float auxDenominador = 0;	
						for(int l = 0; l < getDatosProblema().getNumCriterios(); l++) {
							//System.out.println("Prueba");
							float aux = Math.abs(
									getSolucionElectre().get(i).get(l) - 
									getSolucionElectre().get(j).get(l));
							if(auxDenominador < aux) {
								auxDenominador = aux;
								//System.out.println("Elegido Denominador iteracion "  + i + " " + j + " " + l + " valor " + auxDenominador); 
							}
							//System.out.println("Se ha comparado Denominador" + getSolucionElectre().get(i).get(l) + " " + getSolucionElectre().get(j).get(l));

							//System.out.println("Elegido Denominador iteracion "  + i + " " + l + " valor " + auxDenominador); 
						}
						filaDiscordancia.add(auxNumerador/auxDenominador);
					} else {
						System.out.println(getPermutaciones().get(auxPermutacion).getX() + " " + getPermutaciones().get(auxPermutacion).getY());
						System.out.println("i j " + i + j);
						System.err.println("Error faltan permutaciones");
					}
					auxPermutacion++;
				} else 
					filaDiscordancia.add(0f);	
			}
			getMatrizDiscordancia().add(filaDiscordancia);
		}
	}

	public void calcularMatrizDominanciaConcordancia() {

		matrizDominanciaConcordancia = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
			ArrayList<Integer> aux = new ArrayList<Integer>();
			for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
				if(i == j) {
					aux.add(-1); //No valido
				}
				else if(getMatrizConcordancia().get(i).get(j) >= umbralConcordancia) 
					aux.add(1);
				else 
					aux.add(0);
			}
			getMatrizDominanciaConcordancia().add(aux);
		}
	}

	public void calcularMatrizDominanciaDiscordancia() {
		matrizDominanciaDiscordancia = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
			ArrayList<Integer> aux = new ArrayList<Integer>();
			for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
				if(i == j) {
					aux.add(-1); //No valido
				}
				else if(getMatrizDiscordancia().get(i).get(j) <= umbralDiscordancia) 
					aux.add(1);
				else 
					aux.add(0);
			}
			getMatrizDominanciaDiscordancia().add(aux);
		}

	}

	public void calcularMatrizDominanciaAgregada() {
		matrizDominanciaAgregada = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) {
			ArrayList<Integer> aux = new ArrayList<Integer>();
			for(int j = 0; j < getDatosProblema().getNumAlternativas(); j++) {
				if(i != j) {
					if(getMatrizDominanciaDiscordancia().get(i).get(j) == 1 && getMatrizDominanciaConcordancia().get(i).get(j) == 1) {
						aux.add(1);
					} else 
						aux.add(0);
				} else 
					aux.add(-1);

			}
			getMatrizDominanciaAgregada().add(aux);
		}
	}

	//A cada elemento le asigno un valor 1, los elementos que tengan mayores uniones en la matriz dominancia agregada, se le sumara.
	/* Ej:
	 * -1 1 1
	 * 0 -1 0
	 * 0 1 -1
	 * 
	 * A = 2 + 1
	 * B = 1
	 * C = 1 + 1
	 * Sumamos = 6
	 * A = 50%
	 * B = 16%
	 * C = 33%
	 */

	public void calcularPrioridadesFinales() {

		prioridadesFinales = new ArrayList<Float>();

		int sumaGlobal = getDatosProblema().getNumAlternativas(); //minimo valor

		for(int i = 0; i < getMatrizDominanciaAgregada().size(); i++) {

			int suma = 1; //Prioridad individual
			for(int j = 0; j < getMatrizDominanciaAgregada().size(); j++) {
				if((i != j) && (getMatrizDominanciaAgregada().get(i).get(j) == 1)) {
					suma++; sumaGlobal++;
				}
			}
			getPrioridadesFinales().add((float) suma);
		}

		//showPrioridadesFinales();

		//Normalizamos
		for(int i = 0; i < getPrioridadesFinales().size(); i++) {
			getPrioridadesFinales().set(i, getPrioridadesFinales().get(i) / sumaGlobal);
		}

		//showPrioridadesFinales();

	}

	//SHOWS
	public void showSolucionElectre() {
		if(getSolucionElectre().size() > 0) {
			for(int i = 0; i < getSolucionElectre().size(); i++) {
				for(int j = 0; j < getSolucionElectre().get(i).size(); j++) {
					float f = getSolucionElectre().get(i).get(j);
					double d = f;
					System.out.print((Math.floor((d * 1000)) / 1000) + " ");
				}
				System.out.println();
			}	
		} else {
			System.out.println("No se ha calculado aun la solucion");
		}
	}

	public void showConjuntosConcordanciaDiscordancia() {
		System.out.println("Concordancias:\n");
		for(int i = 0; i < getConjuntoConcordancia().size(); i++) {
			System.out.println("Concordancias:\n");
			System.out.println(getPermutaciones().get(i).x + " " + getPermutaciones().get(i).y + " : " + (getConjuntoConcordancia().get(i)));
			System.out.println("\nDiscordancias: \n");
			System.out.println(getPermutaciones().get(i).x + " " + getPermutaciones().get(i).y + " : " + getConjuntoDiscordancia().get(i));

		}
	}

	public void showMatricesConcordanciaDiscordancia() {

		//Concordancias
		if(getMatrizConcordancia().size() > 0) {
			System.out.println("Matriz Concordancias:\n");
			for(int i = 0; i < getMatrizConcordancia().size(); i++) {
				for(int j = 0; j < getMatrizConcordancia().get(i).size(); j++) 
					System.out.printf("%.4f ", getMatrizConcordancia().get(i).get(j));

				System.out.println();
			}
		} else 
			System.err.println("Error, matriz concordancias");

		//Discordancias
		if(getMatrizDiscordancia().size() > 0) {
			System.out.println("\nMatriz Discordancias:\n");
			for(int i = 0; i < getMatrizDiscordancia().size(); i++) {
				for(int j = 0; j < getMatrizDiscordancia().get(i).size(); j++) 
					System.out.printf("%.4f ", getMatrizDiscordancia().get(i).get(j));

				System.out.println();
			}
		} else 
			System.err.println("Error, matriz discordancias");
	}

	public void showMatricesDominancia() {

		System.out.println("Matriz Dominancia por Concordancia");
		for(int i = 0; i < getMatrizDominanciaConcordancia().size(); i++) {
			for(int j = 0; j < getMatrizDominanciaConcordancia().get(0).size(); j++) {
				System.out.printf("%d ", getMatrizDominanciaConcordancia().get(i).get(j));
			}
			System.out.println();
		}

		System.out.println("Matriz Dominancia por Discordancia");
		for(int i = 0; i < getMatrizDominanciaDiscordancia().size(); i++) {
			for(int j = 0; j < getMatrizDominanciaDiscordancia().get(0).size(); j++) {
				System.out.printf("%d ", getMatrizDominanciaDiscordancia().get(i).get(j));
			}
			System.out.println();
		}
	}

	public void showMatrizDominanciaAgregada() {
		System.out.println("Matriz Dominancia Agregada");
		for(int i = 0; i < getMatrizDominanciaAgregada().size(); i++) {
			for(int j = 0; j < getMatrizDominanciaAgregada().get(0).size(); j++) {
				System.out.printf("%d ", getMatrizDominanciaAgregada().get(i).get(j));
			}
			System.out.println();
		}
	}

	public void showPrioridadesFinales() {
		System.out.println("Electre: " + getImportanciaRelativa().getNombre());
		System.out.println("Prioridades finales");
		for(int i = 0; i < getPrioridadesFinales().size(); i++) {
			System.out.println("Alternativa " + i + " con prioridad " + getPrioridadesFinales().get(i));
		}
	}

	public lectorProblema getDatosProblema() {
		return datosProblema;
	}

	public importanciaRelativaIndividual getImportanciaRelativa() {
		return importanciaRelativa;
	}

	public ArrayList<ArrayList<Float>> getSolucionElectre() {
		return solucionElectre;
	}

	public void setDataSolucionElectre(int i, int j, float dato) {
		solucionElectre.get(i).set(j, dato);
	}

	public ArrayList<ArrayList<Integer>> getConjuntoConcordancia() {
		return conjuntoConcordancia;
	}

	public ArrayList<ArrayList<Integer>> getConjuntoDiscordancia() {
		return conjuntoDiscordancia;
	}

	public ArrayList<ArrayList<Float>> getMatrizConcordancia() {
		return matrizConcordancia;
	}

	public ArrayList<ArrayList<Float>> getMatrizDiscordancia() {
		return matrizDiscordancia;
	}

	public ArrayList<ArrayList<Integer>> getMatrizDominanciaConcordancia() {
		return matrizDominanciaConcordancia;
	}

	public ArrayList<ArrayList<Integer>> getMatrizDominanciaDiscordancia() {
		return matrizDominanciaDiscordancia;
	}


	public ArrayList<ArrayList<Integer>> getMatrizDominanciaAgregada() {
		return matrizDominanciaAgregada;
	}

	public ArrayList<Float> getPrioridadesFinales() {
		return prioridadesFinales;
	}

	public ArrayList<Point> getPermutaciones() {
		return permutaciones;
	}

	public ArrayList<Point> calcularPermutaciones() {
		vector = new float[getDatosProblema().getNumAlternativas()];
		permutaciones = new ArrayList<Point>();

		for(int i = 0; i < getDatosProblema().getNumAlternativas(); i++) 
			vector[i] = i;
		//System.out.println(Perm2(vector, "", 2, getDatosProblema().getNumAlternativas()));
		return new ArrayList<Point>(Perm2(vector, "", 2, getDatosProblema().getNumAlternativas()));

	}
	private ArrayList<Point> Perm2(float[] elem, String act, int n, int r) {

		if (n == 0) {
			String[] aux = act.split(",\\s+");
			//System.out.println("aux " + aux.length + " " + aux[0] + " " + aux[1]) ;
			if(aux.length == 2) {
				getPermutaciones().add(new Point(Math.round(Float.parseFloat(aux[0])), Math.round(Float.parseFloat(aux[1]))));
			} else {
				System.out.println("Error, tamaño de permutaciones");
			}
		} else {
			for (int i = 0; i < r; i++) {
				if (!act.contains(Float.toString(elem[i]))) { // Controla que no haya repeticiones
					Perm2(elem, act + elem[i] + ", ", n - 1, r);
				}
			}
		}
		return getPermutaciones();
	}

}

//delaySeconds = getProperties().getIntProperty("delay.seconds", 10);