/*
 * Copyright (C) 2007-2011 Rivet Logic Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.rivetlogic.export.deployers;

import org.apache.log4j.Logger;
import org.rivetlogic.export.deployers.exceptions.DeployerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalDeployer implements Deployer {
	private String deployTo;
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String deploy(File file, String destinationPath) throws DeployerException {
		logger.debug(String.format("deploy: file '%s' destinationPath '%s'", file.getAbsolutePath(), destinationPath));
		File destFolder = new File(deployTo + destinationPath.substring(0, destinationPath.lastIndexOf(File.separatorChar)));
		destFolder.mkdirs();
		File newFile = new File(deployTo + destinationPath);
		

		/* try copy, in unix renameTo doesn't work across different file systems */
		try {
			copyFile(file, newFile);
		} catch (IOException e) {
			throw new DeployerException(id, "Could not move file to: " + deployTo + destinationPath);
		}
		
		return newFile.getAbsolutePath();
	}
	
	private void copyFile(File source, File dest) throws IOException {
	
		if(!dest.exists()) {
			dest.createNewFile();
		}
        
		InputStream in = null;
        OutputStream out = null;
        try {
	        logger.debug(String.format("copyFile: source '%s' dest '%s'", source.getAbsolutePath(), dest.getAbsolutePath()));
        	in =  new FileInputStream(source);
        	out = new FileOutputStream(dest);
    
	        // Transfer bytes from in to out
	        byte[] buf = new byte[1024];
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
        }
        finally {
        	in.close();
            out.close();
        }
    }


	public String getDeployTo() {
		return deployTo;
	}

	public void setDeployTo(String deployTo) {
		this.deployTo = deployTo;
	}

	protected Logger logger = Logger.getLogger(LocalDeployer.class);
}
