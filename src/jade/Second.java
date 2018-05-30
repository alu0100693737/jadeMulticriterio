package jade;

import javax.swing.JOptionPane;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Second extends Agent {
	
	/*@Override
	protected void setup() {
		addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
				//Receive the other agent message
				ACLMessage msg = receive();
				if(msg != null) {
					JOptionPane.showMessageDialog(null, "Message recibido: " + msg.getContent());
					
				} else block();
			}
		});
	}*/

}
