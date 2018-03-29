package jade;

import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.core.Agent;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Principal {	
	private static void addStationToRuntime(Runtime rt){
		ProfileImpl pContainer = new ProfileImpl(false);
		AgentContainer cont = rt.createAgentContainer(pContainer);

		try {
			cont.createNewAgent("CleaningFloor", "jade.SegundoAgente", null);
			cont.createNewAgent("PaintingFloor", "jade.SegundoAgente", null);
			cont.createNewAgent("SourcePalette", "jade.SegundoAgente", null);
			cont.createNewAgent("GoalPalette", "jade.SegundoAgente", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}

	private static void addTransporterContainer(Runtime rt){
		ProfileImpl pContainer = new ProfileImpl(false);
		AgentContainer cont = rt.createAgentContainer(pContainer);
		try {
			cont.createNewAgent("Transporter", "jade.AgenteTipoElectre", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}

	private static void addCleanerContainer(Runtime rt){
		ProfileImpl pContainer = new ProfileImpl(false);
		AgentContainer cont = rt.createAgentContainer(pContainer);
		try {
			cont.createNewAgent("Cleaner", "jade.PrimerAgente", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}

	private static void addPainterContainer(Runtime rt){
		ProfileImpl pContainer = new ProfileImpl(false);
		AgentContainer cont = rt.createAgentContainer(pContainer);
		try {
			cont.createNewAgent("Painter", "jade.SegundoAgente", null);
		} catch (StaleProxyException e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Runtime rt = Runtime.instance();

		// Exit the JVM when there are no more containers around
		//rt.setCloseVM(true);

		// Create a default profile
		Properties props = new ExtendedProperties();
	    props.setProperty(Profile.GUI, "true");
	  
		AgentContainer mainContainer = rt.createMainContainer(new ProfileImpl(props));

		addStationToRuntime(rt);
		addTransporterContainer(rt);
		addCleanerContainer(rt);
		addPainterContainer(rt);

		AgentController rma = null;
		try {
			rma = mainContainer.createNewAgent("agente", "jade.problemaMulticriterioAgenteModerador", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

	}
}
/*
	public static void main(String[] args) throws Exception {

		//Creacion de agentes usando código
		String[] args1 = new String[3];
	    args1[0] = "-gui";
	    args1[1] = "-agents";
	    args1[2] = "moderador:jade.problemaMulticriterioAgenteModerador";
	    //jade.Boot.main(args1);  

	    Runtime rt = Runtime.instance();
        // Création du profil par défault
    ProfileImpl p = new ProfileImpl(false);
    AgentContainer container =rt.createMainContainer(p);
        // Agent controleur pour permettre la création des agents 
    AgentController Agent=null;
        Agent = container.createNewAgent("Agent1", "jade.problemaMulticriterioAgenteModerador", null);
    Agent.start();      

	}*/
