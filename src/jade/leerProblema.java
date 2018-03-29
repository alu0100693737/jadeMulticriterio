package jade;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class leerProblema {

	private int numAlternativas_;
	private int numCriterios_;
	private ArrayList<String> nombreAtributos_;
	private ArrayList<Boolean> maxminAtributos_; //1 para max, 0 para min
	private ArrayList<ArrayList<Float>> valoresAtributos_;

	public leerProblema(String archivo) throws IllegalArgumentException, Exception {
		maxminAtributos_ = new ArrayList<Boolean>();
		valoresAtributos_ = new ArrayList<ArrayList<Float>>();

		leerFichero(archivo);

		showFichero();
	}

	private void leerFichero(String archivo)  throws IllegalArgumentException, Exception{
		String cadena;
		BufferedReader b = null;
		try {
			FileReader f = new FileReader(archivo);
			b = new BufferedReader(f);
			cadena = b.readLine();

			//Primera linea -> alternativas, atributos y si existe nombre de atributos 
			String[] splited = cadena.split("\\s+");
			if(splited.length == 3) {

				setNumAlternativas(Integer.parseInt(splited[0]));
				setNumCriterios(Integer.parseInt(splited[1]));

				//Existe nombre para los atributos del problema
				if(Integer.parseInt(splited[2]) == 1) {
					nombreAtributos_ = new ArrayList<String>();

					cadena = b.readLine();
					splited = cadena.split("\\s+");
					if(splited.length == getNumCriterios()) {
						for(int i = 0; i < getNumCriterios(); i++) 
							getArrayNombreAtributos().add(splited[i]);
					} else {
						System.err.println("Error en el fichero, la linea de atributos debe tener el mismo lenght que se indica en la primera linea");
						b.close();
						return;
					}	
				}
			} else {
				System.err.println("Error en el fichero, la primera linea debe contener: \n\tNumAlternativas. \n\tNumAtributos. \n\t");
				b.close();
				return;
			}

			//Atributos max o min
			cadena = b.readLine();
			splited = cadena.split("\\s+");
			if(splited.length == getNumCriterios()) {
				for(int i = 0; i < getNumCriterios(); i++) {
					getArrayMaxMinAtributos().add(intToBool(Integer.parseInt(splited[i])));
					System.out.println("splited vale " + splited[i]);
				}
			} else {
				System.err.println("Error en el fichero, la linea de atributos debe tener el mismo lenght que se indica en la primera linea");
				b.close();
				return;
			}	

			//Valor de los atributos para cada alternativa
			while((cadena = b.readLine()) != null) {
				splited = cadena.split("\\s+");
				ArrayList<Float> array = new ArrayList<Float>();
				if(splited.length == getNumCriterios()) {
					for(int i = 0; i < splited.length; i++)
						array.add(Float.parseFloat(splited[i]));
					getArrayValoresAtributos().add(array);
				} else { 
					System.err.println("Error leyendo los atributos del fichero");
					b.close();
					return;
				}
			}
		} catch(Exception E) {
			System.err.println("No se ha podido leer el fichero");
		} finally {
			if(b != null)
				b.close();
		}
	}


	public void showFichero() {
		System.out.println("Fichero de entrada: ");
		System.out.println(getNumAlternativas() + " " + getNumCriterios());
		if(getArrayNombreAtributos().size() > 0) {
			for(int i = 0; i < getArrayNombreAtributos().size(); i++) {
				System.out.print(getArrayNombreAtributos().get(i) + " ");
			}
			System.out.println();
		}

		for(int i = 0; i < getArrayMaxMinAtributos().size(); i++) {
			System.out.print(getArrayMaxMinAtributos().get(i) + " ");
		}
		System.out.println();

		for(int i = 0; i < getArrayValoresAtributos().size(); i++) {
			for(int j = 0; j < getNumCriterios(); j++) 
				System.out.print(getArrayValoresAtributos().get(i).get(j) + " ");
			System.out.println();
		}
	}

	public static boolean intToBool(int input) throws Exception {
		if (input < 0 || input > 1) {
			throw new Exception("input must be 0 or 1");
		}

		// Note we designate 1 as true and 0 as false though some may disagree
		return input == 1;
	}

	public int getNumAlternativas() {
		return numAlternativas_;
	}

	public void setNumAlternativas(int valor) {
		numAlternativas_ = valor;
	}

	public int getNumCriterios() {
		return numCriterios_;
	}

	public void setNumCriterios(int valor) {
		numCriterios_ = valor;
	}

	public ArrayList<String> getArrayNombreAtributos() {
		return nombreAtributos_;
	}

	public ArrayList<Boolean> getArrayMaxMinAtributos() {
		return maxminAtributos_;
	}

	public ArrayList<ArrayList<Float>> getArrayValoresAtributos() {
		return valoresAtributos_;
	}

	public void setArrayValoresAtributo(ArrayList valores) {
		getArrayValoresAtributos().add(valores);
	}
}

