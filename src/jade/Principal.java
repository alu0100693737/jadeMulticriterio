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
	
	public static void main(String[] args) {
		//Creador de container, el constructor crea el moderador.
		//Este cuando lea los ficheros creara los demas agentes
		creadorAgentesContainers prueba = new creadorAgentesContainers("Moderador");
		
	}
}
