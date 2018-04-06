package jade;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
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

	private int[] conjuntoCombinaciones;
	private float[] vector; 
	ArrayList<Point> permutaciones;

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
			calcularMatrizDecisionPonderada();
			showSolucionElectre();
			
			calcularConjuntoConcordanciaDiscordancia();
			showConjuntosConcordanciaDiscordancia();
			
			calcularMatrizConcordancia();
			
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
					System.out.println("Comparamos : " + getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) + " " + getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j));
					if(getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) > getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j)) {
						if(getDatosProblema().getArrayMaxMinAtributos().get(j) == true) {
							//System.out.println("Concordancia");
							auxConcordancia.add(j + 1);
						} else {
							//System.out.println("Discordancia");
							auxDiscordancia.add(j + 1);
						}
					} else if (getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) < getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j)) {
						if(getDatosProblema().getArrayMaxMinAtributos().get(j) == true) {
							//System.out.println("Discordancia");
							auxDiscordancia.add(j + 1);
						} else {
							//System.out.println("Concordancia");
							auxConcordancia.add(j + 1);
						}
					} else {
						//Son valores iguales, misma concordancia y discordancia
						auxConcordancia.add(j + 1);
						auxDiscordancia.add(j + 1);
						System.out.println("Valores iguales, MIRAR");
						//System.out.println(getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).x).get(j) + " " + getDatosProblema().getArrayValoresAtributos().get(permutaciones.get(i).y).get(j));
					}		
				}

				getConjuntoConcordancia().add(auxConcordancia);
				getConjuntoDiscordancia().add(auxDiscordancia);
			}
		}

		
		public void calcularMatrizConcordancia() {
			System.out.println("Empezando matriz concordancia");
		}

		public void calcularMatrizDiscordancia() {

		}

		public void calcularMatrizDominanciaConcordancia() {

		}

		public void calcularMatrizDominanciaDiscordancia() {

		}

		public void calcularMatrizDominanciaAgregada() {

		}

		public void eliminarMenosFavorables() {

		}

	}
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
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

	public void showSolucionElectre() {
		if(getSolucionElectre().size() > 0) {
			for(int i = 0; i < getSolucionElectre().size(); i++) {
				for(int j = 0; j < getSolucionElectre().get(i).size(); j++) {
					System.out.printf("%.2f ", getSolucionElectre().get(i).get(j));
				}
				System.out.println();
			}	
		} else {
			System.out.println("No se ha calculado aun la solucion");
		}
	}

	public ArrayList<ArrayList<Integer>> getConjuntoConcordancia() {
		return conjuntoConcordancia;
	}

	public ArrayList<ArrayList<Integer>> getConjuntoDiscordancia() {
		return conjuntoDiscordancia;
	}

	public void showConjuntosConcordanciaDiscordancia() {
		System.out.println("Concordancias:\n");
		for(int i = 0; i < getConjuntoConcordancia().size(); i++) {
			System.out.println("Concordancias:\n");
			System.out.println(getPermutaciones().get(i).x + " " + getPermutaciones().get(i).y + " : " + (getConjuntoConcordancia().get(i)));
			System.out.println("Discordancias: \n");
			System.out.println(getPermutaciones().get(i).x + " " + getPermutaciones().get(i).y + " : " + getConjuntoDiscordancia().get(i));

		}
	}

	public ArrayList<Point> getPermutaciones() {
		return permutaciones;
	}
	
	public ArrayList<Point> calcularPermutaciones() {
		conjuntoCombinaciones = new int[getDatosProblema().getNumAlternativas()];
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
			System.out.println("aux " + aux.length + " " + aux[0] + " " + aux[1]) ;
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