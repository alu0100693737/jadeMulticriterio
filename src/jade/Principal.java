package jade;

public class Principal {
	
	public static void main(String[] args) throws IllegalArgumentException, Exception {
		
		//Creacion de agentes usando código
		String[] args1 = new String[3];
	    args1[0] = "-gui";
	    args1[1] = "-agents";
	    args1[2] = "moderador:jade.problemaMulticriterioAgenteModerador";
	    jade.Boot.main(args1);  

	}
}
