package jade;

import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.core.Profile;
import jade.wrapper.AgentContainer;

public class creadorSistemaJadeModerador {

	private static Runtime rt;								// Necesario para la ejecucion de Jade	
	private AgentController controllerModerador; 			// Start Agent
	private Properties propiedadesContainersModerador;		// Propiedades del moderador, nombre y -gui
	private static AgentContainer moderadorContainer; 		// Almacena el container main, moderador
	
	public creadorSistemaJadeModerador(String nombre) {
		try {
	
			rt = Runtime.instance();

			//Propiedad nombre y -gui
			setPropiedadesModerador(nombre);
			
			moderadorContainer = getRunTime().createMainContainer(new ProfileImpl(getPropiedadesModerador()));
			controllerModerador = null;

			try {
				controllerModerador = getContenedorModerador().createNewAgent("agenteModerador", "jade.agenteModeradorProblemaMulticriterio", new Object[0]);
				getController().start();
			} catch (Exception e) {
				System.err.println("Error en el creador sistema agente moderador");
				return;			
			}
		}catch (Exception e) {
			System.err.println("Error en el creador sistema agente moderador");
			return;			
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

	public static AgentContainer getContenedorModerador() {
		return moderadorContainer;
	}

	public AgentController getController() {
		return controllerModerador;
	}
}
