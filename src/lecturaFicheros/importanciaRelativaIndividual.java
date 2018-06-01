package lecturaFicheros;

import java.util.ArrayList;

/**
 * Clase importanciaRelativaIndividual
 * Almacena la preferencia individual de un usuario con respecto a un problema multicriterio, almacena nombre y 
 * ponderaciones
 * @author Ivan Garcia Campos   alu0100693737@ull.edu.es
 * @version 1.0, 29/03/2018
 * Asignatura "Sistemas Inteligentes Avanzados"
 * Master en Ingeniería Informática por la ULL
 */
public class importanciaRelativaIndividual {
	/**
	 * Nombre del usuario
	 */
	private String nombre;
	/**
	 * Conjunto de ponderaciones, importancia
	 */
	ArrayList<Float> importancias;
	
	/**
	 * Constructor de la clase importanciaRelativaIndividual
	 * @param datos 			Conjunto de datos, importancia del usuario
	 * @param tamanoCorrecto 	Numero de atributos/criterios del problema. Comprobación fichero correcto
	 */
	public importanciaRelativaIndividual(String datos, int tamanoCorrecto) {
		importancias = new ArrayList<Float>();
		leerImportancia(datos, tamanoCorrecto);
	}
	
	/**
	 * Constructor copia, utilizado para que los agentes tengan los datos correspondientes
	 * @param datos importanciaRelativaIndividual
	 */
	public importanciaRelativaIndividual(Object datos) {
		nombre = ((importanciaRelativaIndividual) datos).getNombre();
		importancias = new ArrayList<Float>(((importanciaRelativaIndividual) datos).getImportancias());
		
		//showImportancias();
	}
	
	/**
	 * Metodo para la lectura del parametro datos y almacenamiento en estructura de la clase
	 * @param datos				Datos del usuario	
	 * @param tamanoCorrecto	Numero de atributos del problema. Comprobación fichero correcto
	 */
	private void leerImportancia(String datos, int tamanoCorrecto) {
		String[] splited = datos.split("\\s+");
		//La suma de las ponderaciones debe ser 1. Ej: 0.2 + 0.1 + 0.1 + 0.1 + 0.2 + 0.3
		float comprobacionSuma = 0;
		
		nombre = splited[0];
		for(int i = 1; i < splited.length; i++) {
			comprobacionSuma += Float.parseFloat(splited[i]);
			getImportancias().add(Float.parseFloat(splited[i]));
		}
		if(getImportancias().size() != (tamanoCorrecto)) {
			System.out.println("Tamano a " + getImportancias().size() + " " + tamanoCorrecto);
			System.err.println("Error en el fichero de importancias relativas, faltan atributos por ponderar");
			return;
		} else if (comprobacionSuma != 1.0f) {
			System.out.println(nombre);
			System.err.println("Ponderaciones incorrectas, fichero mal estructurado, nombre: " + nombre);
			return;
		}
	}
	
	/**
	 * Metodo que muestra la información del usuario y sus importancias/pesos
	 */
	
	public void modificarImportanciaRelativa(int index, float valor) {
		importancias.set(index, valor);
	}
	
	public void showImportancias() {
		System.out.print("\nNombre: " + getNombre() + " ");
		for(int i = 0; i < getImportancias().size(); i++) {
			System.out.print(getImportancias().get(i) + " ");
		}
		System.out.println();
	}
	
	/************************ GETS y SETS ************************/

	/**
	 * Metodo que devuelve el nombre del usuario
	 * @return nombre_
	 */
	public String getNombre() {
		return nombre;
	}
	
	/**
	 * Metodo que devuelve el conjunto de importancias/pesos del usuario
	 * @return
	 */
	public ArrayList<Float> getImportancias() {
		return importancias;
	}
}
