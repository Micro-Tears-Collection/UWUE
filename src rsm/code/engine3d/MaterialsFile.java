package code.engine3d;

import code.utils.IniFile;
import code.utils.assetManager.AssetManager;
import code.utils.assetManager.ReusableContent;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Roman Lahin
 */
public class MaterialsFile extends ReusableContent {
	
	private IniFile ini;
	private Hashtable defaultVals;
	
	public MaterialsFile(String path) {
		lock(); //never unload material files
		//todo unload material files if no materials from it are used
		
		File f = new File("data", path);
		if(f.exists()) {
			ini = AssetManager.loadIni(path, true);
			
			int index = search(ini.keys(), "default");
			if(index >= 0) defaultVals = ini.hashtables()[index];
		}
	}
	
	public void copyMaterialIni(String matPath, IniFile matIni) {
		String matName = matPath.substring(matPath.lastIndexOf('/') + 1);
		
		if(ini != null && ini.groupExists(matName)) {
			int index = search(ini.keys(), matName);
			Hashtable matTable = ini.hashtables()[index];

			Enumeration optionNames = matTable.keys();
			Enumeration options = matTable.elements();

			for(int x=0; x<matTable.size(); x++) {
				String optionName = (String) optionNames.nextElement();
				String option = (String) options.nextElement();

				if(matIni.get(optionName) == null) {
					matIni.put(optionName, option);
				}
			}
		}
		
		if(defaultVals != null) {
			Enumeration optionNames = defaultVals.keys();
			Enumeration options = defaultVals.elements();

			for(int x=0; x<defaultVals.size(); x++) {
				String optionName = (String) optionNames.nextElement();
				String option = (String) options.nextElement();

				if(matIni.get(optionName) == null) {
					matIni.put(optionName, option);
				}
			}
		}
		
		if(matIni.get("albedo") == null) matIni.put("albedo", matPath + ".png");
	}
	
	private int search(String[] keys, String key) {
		for(int i=0; i<keys.length; i++) {
			if(keys[i].equals(key)) return i;
		}
		
		return -1;
	}

}
