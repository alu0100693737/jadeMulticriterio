package jade;

public class Principal {	
	
	public static void main(String[] args) {
		//Creador de container, el constructor crea el moderador.
		//Este cuando lea los ficheros creara los demas agentes
		creadorSistemaJadeModerador prueba = new creadorSistemaJadeModerador("Moderador");

		/*String[] args1 = new String[2];
		args1[0] = "-gui";
		args1[1] = "agenteModerador:jade.agenteModeradorProblemaMulticriterio";
		String[] args2 = new String[2];
		args2[0] = "-container";
		args2[1] = "Electre1:jade.agenteTipoElectre";
		jade.Boot.main(args1);
		jade.Boot.main(args2);*/
	}
}
