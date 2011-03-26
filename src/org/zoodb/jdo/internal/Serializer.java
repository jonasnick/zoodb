package org.zoodb.jdo.internal;

import java.io.IOException;

public class Serializer {

	public static void serializeSchema(Node n, ZooClassDef schema, 
			long oid, SerialOutput out) {
		//write OID
		Session.assertOid(oid);
		out.writeLong(oid);
		
		//write class
		write(out, schema.getClassName());
		
		//write super class
		out.writeLong(schema.getSuperOID());

		//write fields
		ZooFieldDef[] fields = schema.getFields();
		out.writeInt(fields.length);
		
		for (ZooFieldDef f: fields) {
			//Name, type, isPC
			write(out, f.getName());
			write(out, f.getTypeName());
			out.writeBoolean(f.isPersistentType());
		}
	}
	
	
	public static ZooClassDef deSerializeSchema(Node node, SerialInput in) {
		//read OID
		long oid = in.readLong();
		
		//read class
		String className = readString(in);
		long supOid = in.readLong();
		
		//read fields
		int nF = in.readInt();
		String[] fNames = new String[nF];
		String[] tNames = new String[nF];
		boolean[] isPCs = new boolean[nF];
		
		for (int i = 0; i < nF; i++) {
			fNames[i] = readString(in);
			tNames[i] = readString(in);
			isPCs[i] = in.readBoolean();
		}
		
		Class<?> cls;
		try {
			cls = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class not found: \"" + className + "\"", e);
		}

		ZooClassDef sch = new ZooClassDef(cls, oid, null, supOid);
		return sch;
	}
	
	
	public static void serializeUser(User user, SerialOutput out) throws IOException {
	    out.writeInt(user.getID());
	    
        //write flags
        out.writeBoolean(user.isDBA());// DBA=yes
        out.writeBoolean(user.isR());// read access=yes
        out.writeBoolean(user.isW());// write access=yes
        out.writeBoolean(user.isPasswordRequired());// passwd=no
        
        //write name
        out.writeString(user.getNameDB());
        
        //use CRC32 as basic password encryption to avoid password showing up in clear text.
        out.writeLong(user.getPasswordCRC());
	}
	
	
	public static User deSerializeUser(SerialInput in, Node node, int userID) throws IOException {
        String uNameOS = System.getProperty("user.name");
        User user = new User(uNameOS, userID);
        
        //read flags
        user.setDBA( in.readBoolean() );// DBA=yes
        user.setR( in.readBoolean() );// read access=yes
        user.setW( in.readBoolean() );// write access=yes
        user.setPasswordRequired( in.readBoolean() );// passwd=no
        
        //read name
        user.setNameDB( in.readString() );
        
        //use CRC32 as basic password encryption to avoid password showing up in clear text.  
        long uPassWordCRC = in.readLong(); //password CRC32
        user.setPassCRC(uPassWordCRC);
		
		return user;
	}
	
	
	private static String readString(SerialInput in) {
		return in.readString();
	}


	private static final void write(SerialOutput out, String str) {
		out.writeString(str);
	}
}
