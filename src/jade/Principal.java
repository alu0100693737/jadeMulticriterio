package jade;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Principal {

	private static Agent agentePrueba;
	
	public static void main(String[] args) {
		
		//Creacion de agentes usando código
		String[] args1 = new String[3];
	    args1[0] = "-gui";
	    args1[1] = "-agents";
	    args1[2] = "prueba:jade.PrimerAgente";
	    jade.Boot.main(args1);  

	}
}
