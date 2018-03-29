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
	
	public leerProblema() {
		// TODO Auto-generated constructor stub
	}

	private void leerFichero(String archivo)  throws FileNotFoundException, IOException{
		String cadena;
		FileReader f = new FileReader(archivo);
		BufferedReader b = new BufferedReader(f);
		cadena = b.readLine();
		String[] splited = cadena.split("\\s+");
		if(splited.length == 3) {
			
			setNumAlternativas(Integer.parseInt(splited[0]));
			setNumCriterios(Integer.parseInt(splited[1]));
			
			//Existe nombre para los atributos del problema
			if(Integer.parseInt(splited[2]) == 1) {
				
			}
		} else {
			System.out.println("Error en el fichero, la primera linea debe contener: \n\tNumAlternativas. \n\tNumAtributos. \n\t");
		}
		
		while((cadena = b.readLine()) != null) {
			
			System.out.println(cadena);
		}
		b.close();
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
	
	public ArrayList<String> getNombreAtributos() {
		return nombreAtributos_;
	}
	
	public ArrayList<Boolean> getMaxMinAtributos() {
		return maxminAtributos_;
	}

}

