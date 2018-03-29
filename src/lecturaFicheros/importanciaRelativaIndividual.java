package lecturaFicheros;

import java.util.ArrayList;

public class importanciaRelativaIndividual {
	private String nombre_;
	ArrayList<Float> importancias_;
	
	public importanciaRelativaIndividual(String datos, int tamanoCorrecto) {
		importancias_ = new ArrayList<Float>();
		leerImportancia(datos, tamanoCorrecto);
	}
	
	public void leerImportancia(String datos, int tamanoCorrecto) {
		String[] splited = datos.split("\\s+");
		//La suma de las ponderaciones debe ser 1. Ej: 0.2 0.1 0.1 0.1 0.2 0.3
		float comprobacionSuma = 0;
		
		nombre_ = splited[0];
		for(int i = 1; i < splited.length; i++) {
			comprobacionSuma += Float.parseFloat(splited[i]);
			getImportancias().add(Float.parseFloat(splited[i]));
		}
		if(getImportancias().size() != (tamanoCorrecto + 1)) {
			System.err.println("Error en el fichero de importancias relativas, faltan atributos por ponderar");
			return;
		} else if (comprobacionSuma != 1.0f) {
			System.err.println("Ponderaciones incorrectas, fichero mal estructurado");
			return;
		}
	}
	
	public String getNombre() {
		return nombre_;
	}
	
	public ArrayList<Float> getImportancias() {
		return importancias_;
	}
}
