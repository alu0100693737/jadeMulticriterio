package jade;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JOptionPane;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.ExtendedProperties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import lecturaFicheros.*;

import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Clase que resuelve problemas multicriterios, agente moderador
 *  * @author Ivan Garcia Campos   alu0100693737@ull.edu.es
 * @version 1.0, 29/03/2018
 * Asignatura "Sistemas Inteligentes Avanzados"
 * Master en Ingeniería Informática por la ULL
 * 
 * Lee los ficheros del problema y crea los agentes necesarios, se intentará comunicación mediante mensaje
 */
public class agenteModeradorProblemaMulticriterio extends Agent {

	/**
	 * Atributo de la clase leerProblema para la lectura del problema
	 */
	private lectorProblema lectorFichero;
	/**
	 * Atributo de la clase leerImportanciasRelativas para la lectura de importancias por usuarios
	 */
	private lectorImportanciasRelativas lectorFicheroImportancias;

	/**
	 * Variable final que almacena el nombre del fichero que contiene los datos del problema
	 */
	public static final String FICHERO_PROBLEMA = "datosProblema2.txt";
	/**
	 * Variable final que almacena el nombre del fichero que contiene las importancias relativas de los usuarios
	 */
	public static final String FICHERO_IMPORTANCIAS_RELATIVAS = "importanciaRelativaPersonalCoches2.txt";

	public static final int NUMERO_AGENTES = 20;
	private float consensoActual;

	private static Runtime rt;	

	private static AgentContainer electreContainer;			// Almacena el container electre, tipo de resolucion de agente
	private static AgentContainer PrometheeContainer;		// Almacena el container Promethee, tipo de resolucion de agente
	private static AgentContainer ahpContainer;				// Almacena el container ahp, tipo de resolucion

	private ArrayList<ArrayList<Float>> prioridadesFinales;
	private ArrayList<String> nombresAgentes;

	private ArrayList<Float> decisionFinal;
	private boolean primeraVuelta;
	/**
	 * Metodo para la inicializacion del agente moderador
	 */
	@Override
	protected void setup() {

		// Leemos los ficheros y creamos los correspondientes electre, Promethee y AHP
		try {
			consensoActual = 0;

			lectorFichero = new lectorProblema(FICHERO_PROBLEMA);
			lectorFicheroImportancias = new lectorImportanciasRelativas(FICHERO_IMPORTANCIAS_RELATIVAS, getLectorFichero().getNumCriterios());

			rt = Runtime.instance();

			//Propiedades de contenedores, solo nombre
			ExtendedProperties propertiesElectred = new ExtendedProperties();
			propertiesElectred.setProperty(Profile.CONTAINER_NAME, "Electre");
			ExtendedProperties propertiesPromethee = new ExtendedProperties();
			propertiesPromethee.setProperty(Profile.CONTAINER_NAME, "Promethee");
			ExtendedProperties propertiesAHP = new ExtendedProperties();
			propertiesAHP.setProperty(Profile.CONTAINER_NAME, "AHP");

			electreContainer = getRunTime().createAgentContainer(new ProfileImpl(propertiesElectred));
			PrometheeContainer = getRunTime().createAgentContainer(new ProfileImpl(propertiesPromethee));
			ahpContainer = getRunTime().createAgentContainer(new ProfileImpl(propertiesAHP));

			primeraVuelta = false;
			//System.out.println("Agente moderador, ya se han leido los ficheros, añadimos Electre");

			//addAgenteTipoElectre(NUMERO_AGENTES, 0);
			//addAgenteTipoPromethee(NUMERO_AGENTES, 0);
			//addAgenteTipoAHP(NUMERO_AGENTES, 0);
			addAgenteTipoElectre(NUMERO_AGENTES/3, 0);
			addAgenteTipoPromethee(NUMERO_AGENTES/3, NUMERO_AGENTES/3);
			addAgenteTipoAHP(NUMERO_AGENTES - (2 * NUMERO_AGENTES/3 - 1), 2 * NUMERO_AGENTES/3 - 1);

			//Nos comunicamos con los agentes buscando la solucion individual
			
			//Inicializamos
			prioridadesFinales = new ArrayList<ArrayList<Float>>();
			nombresAgentes = new ArrayList<String>();
		
			addBehaviour(new CyclicBehaviour() {
				
				@Override
				public void action() {
					//Receive the other agent message
					ACLMessage msg = receive();
					if(msg != null) {
						
						String split[] = msg.getContent().split("\\n");

						//Nombre[0] contiene el nombre
						String nombre[] = split[0].split("@");
						if(primeraVuelta == false) {
							
							
							
							getNombresAgentes().add(nombre[0]);
	
							//Añadimos las prioridades
							ArrayList<Float> valores = new ArrayList<Float>();
							String aux = split[1].substring(1, split[1].length() - 1);
							String[] aux2 = aux.split(",\\s*");
							//System.out.println("Aux " + aux2[0]);
							//System.out.println("Aux length vale " + aux2.length);
							for(int i = 0; i < aux2.length; i++) {
								valores.add(Float.parseFloat(aux2[i]));
							}
	
							getPrioridadesFinales().add(valores);
							
							//Modificaciones
						} else {
												
							int posnombre = getNombresAgentes().indexOf(nombre[0]);
							System.out.println("Recibido modificacion de " + nombre[0] + " elemento " + posnombre);
							
							ArrayList<Float> valores = new ArrayList<Float>();
							String aux = split[1].substring(1, split[1].length() - 1);
							String[] aux2 = aux.split(",\\s*");
							//System.out.println("Aux " + aux2[0]);
							//System.out.println("Aux length vale " + aux2.length);
							for(int i = 0; i < aux2.length; i++) {
								valores.add(Float.parseFloat(aux2[i]));
							}
							System.out.println("Recibido modificacion de " + nombre[0] + " elemento " + posnombre);
							System.out.println("Antes " + getPrioridadesFinales().get(posnombre) + " Despues " + valores);
							getPrioridadesFinales().set(posnombre, valores);
						}

					} else {
						block();
						if(getPrioridadesFinales().size() == NUMERO_AGENTES) {
							if(primeraVuelta == false) {
								JOptionPane.showMessageDialog(null, "Primera vuelta");
							} else {
								JOptionPane.showMessageDialog(null, "Consenso hablando con las personas que estan en desacuerdo");
							}
							primeraVuelta = true;
							System.out.println("Hay un total de " + getNombresAgentes().size() + " que es igual a " + getPrioridadesFinales().size());
							showDatosPrioridadesFinalesPorAgente();
							
							calcularDecisionFinal();
							
							//No se ha encontrado mayoria absoluta, se procede a un consenso
							if(!calcularMayoria()) {
								
								
								for(int i = 0; i < getPrioridadesFinales().size(); i++) {
									if(getMax(getPrioridadesFinales().get(i)).getX() != getMax(getdecisionFinal()).getX()) {

										//System.out.println(getNombresAgentes().get(i) + "La individual es en el punto " + getPrioridadesFinales().get(i));
										//System.out.println("La colectiva es en el punto " + getdecisionFinal());
										//System.out.println("Error en " + getNombresAgentes().get(i));

										//Realizar cambio, nos comunicamos con el agente diciendole que alternativa es el del grupo
										ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
										msg1.setContent(getMax(getPrioridadesFinales().get(i)).getX() + " " + getMax(getdecisionFinal()).getX() + " " + getNombresAgentes().get(i));
										msg1.addReceiver(new AID(getNombresAgentes().get(i), AID.ISLOCALNAME));
										send(msg1);
									}
								}
								
								block();
							} else {
								System.out.println("HECHO!!!!!");
							}
						} else if (primeraVuelta == true) {
							System.out.println("Estamos en la ronda de consenso");
						} else {
							System.out.println("NO SE HAN LEIDO TODOS LOS MENSAJES");
						}
					}
				}
			});


		} catch (Exception e) {
			System.err.println("Error en el agente moderador");
			return;			
		}
	}

	/**
	 * Metodo Override, Agent para la finalizacion del agente
	 */
	protected void takeDown() { 
		System.out.println("Eliminando el agente");
	}


	public void addAgenteTipoElectre(int numAgentes, int inicio) {
		try {
			//Si existen prioridades para asignar a todos los agentes
			if(numAgentes <= getLectorFicheroImportancias().getImportanciasRelativas().size()) {

				for(int i = 0; i < numAgentes; i++) {

					//A cada objeto se le pasan los datos del problema y la prioridad que corresponda
					Object[] args = new Object[2];
					args[0] = new lectorProblema(getLectorFichero());
					args[1] = new importanciaRelativaIndividual(getLectorFicheroImportancias().getImportanciasRelativas().get(i + inicio));

					//tercer elemento, parametros
					String nombre = "Electre" + (i + inicio) + "Procedure";
					AgentController prueba  = getContenedorElectre().createNewAgent(nombre, "jade.agenteTipoElectre", args);
					prueba.start();
				}
			}
		} catch (StaleProxyException e1) {
			System.out.println("Error");
			e1.printStackTrace();
		}
	}

	public void addAgenteTipoPromethee(int numAgentes, int inicio) {
		try {
			//Si existen prioridades para asignar a todos los agentes
			if(numAgentes <= getLectorFicheroImportancias().getImportanciasRelativas().size()) {

				for(int i = 0; i < numAgentes; i++) {

					//A cada objeto se le pasan los datos del problema y la prioridad que corresponda
					Object[] args = new Object[2];
					args[0] = new lectorProblema(getLectorFichero());
					args[1] = new importanciaRelativaIndividual(getLectorFicheroImportancias().getImportanciasRelativas().get(i + inicio));

					//tercer elemento, parametros
					String nombre = "Promethee" + (i + inicio) + "Procedure";
					AgentController prueba  = getContenedorPromethee().createNewAgent(nombre, "jade.agenteTipoPromethee", args);
					prueba.start();
				}
			}
		} catch (StaleProxyException e1) {
			System.out.println("Error");
			e1.printStackTrace();
		}
	}

	public void addAgenteTipoAHP(int numAgentes, int inicio) {
		try {
			//Si existen prioridades para asignar a todos los agentes
			if(numAgentes <= getLectorFicheroImportancias().getImportanciasRelativas().size()) {

				for(int i = 0; i < numAgentes; i++) {

					//A cada objeto se le pasan los datos del problema y la prioridad que corresponda
					Object[] args = new Object[2];
					args[0] = new lectorProblema(getLectorFichero());
					args[1] = new importanciaRelativaIndividual(getLectorFicheroImportancias().getImportanciasRelativas().get(i + inicio));

					//tercer elemento, parametros
					String nombre = "AHP" + (i + inicio) + "Procedure";
					AgentController prueba  = getContenedorAHP().createNewAgent(nombre, "jade.agenteTipoAHP", args);
					prueba.start();
				}
			}
		} catch (StaleProxyException e1) {
			System.out.println("Error");
			e1.printStackTrace();
		}
	}

	public void calcularDecisionFinal() {

		decisionFinal = new ArrayList<Float>();

		//Hacemos la media de todas las opiniones de los agentes
		for(int i = 0; i < getLectorFichero().getNumAlternativas(); i++) {
			float suma = 0f;
			for(int j = 0; j < getPrioridadesFinales().size(); j++) {
				suma += getPrioridadesFinales().get(j).get(i);
			}
			getdecisionFinal().add(suma / NUMERO_AGENTES);
		}
		//System.out.println("!!!Mostrando " + getLectorFichero().getNumAlternativas());
		showDecisionFinal();
	}

	//Devuelve el index y el valor del maximo elemento de un arraylist
	public Point2D.Float getMax(ArrayList<Float> list){
		float max = Integer.MIN_VALUE;
		float index = 0;
		for(int i=0; i<list.size(); i++){
			if(list.get(i) > max){
				max = list.get(i);
				index = i;
			}
		}
		return new Point2D.Float(index, max);
	}

	public boolean calcularMayoria() {
		int suma = 0;
		//Calcula los elementos que tienen la misma primera opcion
		for(int i = 0; i < getPrioridadesFinales().size(); i++) {
			if(getMax(getPrioridadesFinales().get(i)).getX() == getMax(getdecisionFinal()).getX()) {
				System.out.println(getPrioridadesFinales().get(i) + "El primer elemento escogido localmente y globalmente es el mismo en " + i + " " + getdecisionFinal());
				suma += 1;
			}
		}
		
		System.out.println("Valor " + suma);

		setConsensoActual(suma);
		int aux = 0;
		//Mayoria 2/3
		if(getConsensoActual() > (2 * NUMERO_AGENTES / 3)) {
			JOptionPane.showMessageDialog(null, "Se ha encontrado un consenso (Mayoria 2/3) del " +  (getConsensoActual() / (float)NUMERO_AGENTES) + "%. \n Solucion " + getdecisionFinal() + " con numero: " + (getMax(getdecisionFinal()).getX()));
			return true;
		}
		//Mayoria absoluta
		else if(getConsensoActual() > (NUMERO_AGENTES / 2)){ //CONSENSO
			JOptionPane.showMessageDialog(null, "Se ha encontrado un consenso (Mayoria Absoluta) del " +  (getConsensoActual() / (float)NUMERO_AGENTES) + "%. \n Solucion " + getdecisionFinal() + " con numero: " + (getMax(getdecisionFinal()).getX()));
			return true;
		} 
		//Regla de la minoría 2/3. Mitad de expertos de forma aleatoria
		else if((aux = aplicarMinoria()) > (2 * NUMERO_AGENTES / 4 * 3)) {
			JOptionPane.showMessageDialog(null, "Se ha encontrado un consenso (Mayoria 2/3, por minoria) del " +  ((getConsensoActual() / (float)NUMERO_AGENTES)) + "%. -> " + ((float)((float)aux / (float)(NUMERO_AGENTES / 2))) + "%\n Solucion " + getdecisionFinal() + " con numero: " + (getMax(getdecisionFinal()).getX()));
			return false;
		} 
		//Regla de la minoria absoluta. Mitad de expertos
		else if((aux = aplicarMinoria()) > (NUMERO_AGENTES / 4)) {
			JOptionPane.showMessageDialog(null, "Se ha encontrado un consenso (Mayoria Absoluta, por minoria) del " +  (getConsensoActual() / (float)NUMERO_AGENTES) + "%. -> " + ((float)((float)aux / (float)(NUMERO_AGENTES / 2))) + "%\n Solucion " + getdecisionFinal() + " con numero: " + (getMax(getdecisionFinal()).getX()));
			return false;
		} else {
			System.out.println("Saliendo");
			JOptionPane.showMessageDialog(null, "No hay ningun tipo de consenso" +  (getConsensoActual() / (float)NUMERO_AGENTES) + "%. -> " + ((float)((float)aux / (float)(NUMERO_AGENTES / 2))) + "%\n Solucion " + getdecisionFinal() + " con numero: " + (getMax(getdecisionFinal()).getX()));

			//Debe aplicarse otro procedimiento
			return false;
		}

	}

	//Aplicamos la evaluacion a la mitad de los elementos
	public int aplicarMinoria() {
		//System.out.println("\nPor Minoria \n\n");
		ArrayList<Integer> minoriasEscogidas = new ArrayList<Integer>();
		Random rand = new Random();
		int aux;
		for(int i = 0; i < NUMERO_AGENTES / 2; i++) {
			aux = rand.nextInt(NUMERO_AGENTES);
			while(minoriasEscogidas.contains(aux)) {
				aux = rand.nextInt(NUMERO_AGENTES);
			}
			//System.out.println("Añadiendo aux " + aux);
			minoriasEscogidas.add(aux);
		}
		int suma = 0;
		//System.out.println("Prioridades finales " +  getPrioridadesFinales().size());
		for(int i = 0; i < minoriasEscogidas.size(); i++) {
			if(getMax(getPrioridadesFinales().get(minoriasEscogidas.get(i))).getX() == getMax(getdecisionFinal()).getX()) {
				//System.out.println("El primer elemento escogido localmente y globalmente es el mismo en " + minoriasEscogidas.get(i));
				suma += 1;
			} else {
				//System.out.println("no coincide");
			}
		}
		System.out.println("Minoria vale " + suma);
		return suma;
	}

	public float getConsensoActual() {
		return consensoActual;
	}

	public void setConsensoActual(float valor) {
		consensoActual = valor;
	}

	public void showDatosPrioridadesFinalesPorAgente() {
		System.out.println("Prioridades finales ");
		for(int i = 0; i < getNombresAgentes().size(); i++) {
			System.out.println("Agente " + getNombresAgentes().get(i) + " " + 
					getLectorFicheroImportancias().getImportanciasRelativas().get(i).getNombre() + 
					" con los valores: " + getPrioridadesFinales().get(i));
		}
	}

	public void showDecisionFinal() {
		System.out.println("Decision final");
		for(int i = 0; i < getdecisionFinal().size(); i++) {
			System.out.printf("%.2f  ", getdecisionFinal().get(i));
		}
		System.out.println();
	}

	/**
	 * Metodo que devuelve el contenedor de agentes Electre
	 * @return AgentContainer
	 */
	public static AgentContainer getContenedorElectre() {
		return electreContainer;
	}

	/**
	 * Metodo que devuelve el contenedor de agentes Promethee
	 * @return AgentContaienr
	 */
	public static AgentContainer getContenedorPromethee() {
		return PrometheeContainer;
	}

	/**
	 * Metodo que devuelve el contenedor de agentes AHP
	 * @return AgentContainer
	 */
	public static AgentContainer getContenedorAHP() {
		return ahpContainer;
	}

	/**
	 * Metodo que devuelve el atributo de la clase leerProblema, lectorFichero_
	 * @return lectorFichnero
	 */
	public lectorProblema getLectorFichero() {
		return lectorFichero;
	}

	/**
	 * Metodo que devuelve el atributo de la clase leerImportanciasRelativas, lectorFicheroImportancias_
	 * @return lectorFicheroImportancias
	 */
	public lectorImportanciasRelativas getLectorFicheroImportancias() {
		return lectorFicheroImportancias;
	}

	/**
	 * Metodo que devuelve las prioridades finales despues de haber sido calculada por todos los agentes.
	 * @return prioridadesFinales
	 */
	public ArrayList<ArrayList<Float>> getPrioridadesFinales() {
		return prioridadesFinales;
	}

	/**
	 * Metodo que devuelve los nombres finales asociados a las prioridades finales
	 * @return nombresAgentes;
	 */
	public ArrayList<String> getNombresAgentes() {
		return nombresAgentes;
	}


	public ArrayList<Float> getdecisionFinal() {
		return decisionFinal;
	}
	/**
	 * Metodo que devuelve el RunTime rt, ejecucion de agentes
	 * @return Runtime
	 */
	public static Runtime getRunTime() {
		return rt;
	}
}
