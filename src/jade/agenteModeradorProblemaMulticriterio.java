package jade;

import java.util.ArrayList;

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
	public static final String FICHERO_PROBLEMA = "datosProblema.txt";
	/**
	 * Variable final que almacena el nombre del fichero que contiene las importancias relativas de los usuarios
	 */
	public static final String FICHERO_IMPORTANCIAS_RELATIVAS = "importanciaRelativaPersonal.txt";

	private static Runtime rt;	

	private static AgentContainer electreContainer;			// Almacena el container electre, tipo de resolucion de agente
	private static AgentContainer PrometheeContainer;		// Almacena el container Promethee, tipo de resolucion de agente
	private static AgentContainer ahpContainer;				// Almacena el container ahp, tipo de resolucion

	/**
	 * Metodo para la inicializacion del agente moderador
	 */
	@Override
	protected void setup() {
		
		//Comportamiento
		addBehaviour(new OneShotBehaviour() {

			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("Testintg");
				msg.setLanguage("Español");
				msg.addReceiver(new AID("Electre1", AID.ISLOCALNAME));
				send(msg);
			}
		});
		
		// Leemos los ficheros y creamos los correspondientes electre, Promethee y AHP
		try {
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

			System.out.println("Agente moderador, ya se han leido los ficheros, añadimos Electre");

			//Añadiendo agentes de cada tipo
			//int num = 1;
			//addAgenteTipoElectre(1);
			addAgenteTipoPromethee(1);
			
			//enviarMensajeAgente();

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


	public void addAgenteTipoElectre(int numAgentes) {
		try {
			//Si existen prioridades para asignar a todos los agentes
			if(numAgentes <= getLectorFicheroImportancias().getImportanciasRelativas().size()) {
				
				for(int i = 0; i < numAgentes; i++) {
					
					//A cada objeto se le pasan los datos del problema y la prioridad que corresponda
					Object[] args = new Object[2];
					args[0] = new lectorProblema(getLectorFichero());
					args[1] = new importanciaRelativaIndividual(getLectorFicheroImportancias().getImportanciasRelativas().get(i));
					
					//tercer elemento, parametros
					String nombre = "Electre" + i + "Procedure";
					AgentController prueba  = getContenedorElectre().createNewAgent(nombre, "jade.agenteTipoElectre", args);
					prueba.start();
				}
			}
		} catch (StaleProxyException e1) {
			System.out.println("Error");
			e1.printStackTrace();
		}
	}

	public void addAgenteTipoPromethee(int numAgentes) {
		try {
			//Si existen prioridades para asignar a todos los agentes
			if(numAgentes <= getLectorFicheroImportancias().getImportanciasRelativas().size()) {
				
				for(int i = 0; i < numAgentes; i++) {
					
					//A cada objeto se le pasan los datos del problema y la prioridad que corresponda
					Object[] args = new Object[2];
					args[0] = new lectorProblema(getLectorFichero());
					args[1] = new importanciaRelativaIndividual(getLectorFicheroImportancias().getImportanciasRelativas().get(i));
					
					//tercer elemento, parametros
					String nombre = "Promethee" + i + "Procedure";
					AgentController prueba  = getContenedorPromethee().createNewAgent(nombre, "jade.agenteTipoPromethee", args);
					prueba.start();
				}
			}
		} catch (StaleProxyException e1) {
			System.out.println("Error");
			e1.printStackTrace();
		}
	}

	public void addAgenteTipoAHP() {
		try {
			getContenedorAHP().createNewAgent("AHP1", "jade.agenteTipoAHP", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Metodo utilizado para enviar mensajes a los agentes desde el moderador
	 * Se intentará enviar la matriz de datos y las prioridades
	 */
	public void enviarMensajeAgente() {
		AID id = new AID();

		id.setLocalName("Electre1");
		ACLMessage mensaje = new ACLMessage(ACLMessage.INFORM);
		mensaje.setSender(getAID());
		mensaje.setLanguage("Español");
		mensaje.addReceiver(id);
		mensaje.setContent("Hola Receptor");
		send(mensaje);
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
	 * @return lectorFicheroImportancias_
	 */
	public lectorImportanciasRelativas getLectorFicheroImportancias() {
		return lectorFicheroImportancias;
	}

	/**
	 * Metodo que devuelve el RunTime rt, ejecucion de agentes
	 * @return Runtime
	 */
	public static Runtime getRunTime() {
		return rt;
	}
}
