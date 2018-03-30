package jade;

import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.core.Profile;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class creadorAgentesContainers {

	private static Runtime rt;								// Necesario para la ejecucion de Jade	
	private AgentController controllerModerador; 			// Start
	private Properties propiedadesContainersModerador;		// Propiedades del moderador, nombre y -gui

	private static AgentContainer moderadorContainer; 		// Almacena el container main, moderador
	private static AgentContainer electreContainer;			// Almacena el container electre, tipo de resolucion de agente
	private static AgentContainer prometheoContainer;		// Almacena el container prometheo, tipo de resolucion de agente
	private static AgentContainer ahpContainer;				// Almacena el container ahp, tipo de resolucion

	public creadorAgentesContainers(String nombre) {
		rt = Runtime.instance();

		//Propiedad nombre y -gui
		setPropiedadesModerador(nombre);
		moderadorContainer = getRunTime().createMainContainer(new ProfileImpl(getPropiedadesModerador()));

		//Propiedades de contenedores, solo nombre
		ExtendedProperties propertiesElectred = new ExtendedProperties();
		propertiesElectred.setProperty(Profile.CONTAINER_NAME, "Electred");
		ExtendedProperties propertiesPrometheo = new ExtendedProperties();
		propertiesPrometheo.setProperty(Profile.CONTAINER_NAME, "Prometheo");
		ExtendedProperties propertiesAHP = new ExtendedProperties();
		propertiesAHP.setProperty(Profile.CONTAINER_NAME, "AHP");

		electreContainer = getRunTime().createAgentContainer(new ProfileImpl(propertiesElectred));
		prometheoContainer = getRunTime().createAgentContainer(new ProfileImpl(propertiesPrometheo));
		ahpContainer = getRunTime().createAgentContainer(new ProfileImpl(propertiesAHP));

		controllerModerador = null;

		try {
			controllerModerador = getContenedorModerador().createNewAgent("agente", "jade.agenteModeradorProblemaMulticriterio", new Object[0]);

			//Añadiendo agentes de cada tipo
			addAgenteTipoElectre();
			addAgenteTipoPrometheo();
			addAgenteTipoAHP();

			getController().start();
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	// MAS EJEMPLOS

	public void addAgenteTipoElectre(){
		try {
			getContenedorElectre().createNewAgent("Electre1", "jade.agenteTipoElectre", null);
			//getContenedorElectre().createNewAgent("PaintingFloor", "jade.agenteTipoElectre", null);
			//getContenedorElectre().createNewAgent("SourcePalette", "jade.agenteTipoElectre", null);
			//getContenedorElectre().createNewAgent("GoalPalette", "jade.agenteTipoElectre", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}

	public void addAgenteTipoPrometheo(){
		try {
			getContenedorPrometheo().createNewAgent("Prometheo1", "jade.agenteTipoPrometheo", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}

	public void addAgenteTipoAHP(){
		try {
			getContenedorAHP().createNewAgent("AHP1", "jade.agenteTipoAHP", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}
	
	/************************ GETS y SETS ************************/

	public Properties getPropiedadesModerador() {
		return propiedadesContainersModerador;
	}

	private void setPropiedadesModerador(String nombre) {
		propiedadesContainersModerador = new ExtendedProperties();
		getPropiedadesModerador().setProperty(Profile.GUI, "true");
		getPropiedadesModerador().setProperty(Profile.CONTAINER_NAME, nombre);
	}

	public static Runtime getRunTime() {
		return rt;
	}

	//Contenedores

	public static AgentContainer getContenedorModerador() {
		return moderadorContainer;
	}

	public static AgentContainer getContenedorElectre() {
		return electreContainer;
	}

	public static AgentContainer getContenedorPrometheo() {
		return prometheoContainer;
	}

	public static AgentContainer getContenedorAHP() {
		return ahpContainer;
	}

	public AgentController getController() {
		return controllerModerador;
	}
}
