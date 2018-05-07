package jade;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.messaging.MatchAllFilter;
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


	private final float umbralConcordancia = 0.55f;
	private final float umbralDiscordancia = 0.71f;

	private float[] vector; 
	private ArrayList<Point> permutaciones;

	@Override
	protected void setup() { 

		System.out.println("\nHola! El agente "+getAID().getName()+" está listo.\n");

		Object[] args = getArguments();         // Obtiene los argumentos dados en la inicialización del comprador
		if (args != null && args.length == 2) {  // Tiene que haber al menos un argumento
			if((args[0] instanceof lectorProblema) && (args[1] instanceof importanciaRelativaIndividual)) {	

				datosProblema = new lectorProblema((lectorProblema) args[0]);
				importanciaRelativa = new importanciaRelativaIndividual((importanciaRelativaIndividual) args[1]);
				System.out.println("\n------------------------------------------------------------------\n");
				addBehaviour(new CyclicBehaviour() {

					@Override
					public void action() {

						ACLMessage msg = receive();
						if(msg != null) {
							System.out.println(msg.getContent());
						} else
							block();
					}
				});
				addBehaviour(new comportamientoElectre());

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
			showSolucionElectre();
			calcularMatrizDecisionPonderada();
			System.out.println("\nMatriz Ponderada\n");
			showSolucionElectre();

			calcularConjuntoConcordanciaDiscordancia();
			showConjuntosConcordanciaDiscordancia();

			calcularMatrizConcordancia();
			calcularMatrizDiscordancia();

			showMatricesConcordanciaDiscordancia();

			calcularMatrizDominanciaConcordancia();
			calcularMatrizDominanciaDiscordancia();
			showMatricesDominancia();

			calcularMatrizDominanciaAgregada();
			showMatrizDominanciaAgregada();

			block();
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
			System.out.println("Empezando matriz concordancia");
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
			System.out.println("Empezando matriz discordancia");
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

		//Se ordenaran por el numero de 1s que tengan 
		public void eliminarMenosFavorables() {

		}

	}

	protected void takeDown() { 
		System.out.println("Eliminando el agente");
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