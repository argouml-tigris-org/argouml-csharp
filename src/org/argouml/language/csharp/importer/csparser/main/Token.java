package org.argouml.language.csharp.importer.csparser.main;

import org.argouml.language.csharp.importer.csparser.enums.TokenID;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 1:47:34 PM
 */
public class Token {
		public int ID;
		public int Data; // index into data table

		public Token(int id)
		{
			this.ID = id;
			this.Data = -1;
		}
		public Token(int id, int data)
		{
			this.ID = id;
			this.Data = data;
		}

		public  String ToString()
		{
			return String.valueOf(this.ID);
		}

//    public boolean equals(Object obj) {
//        Token t=(Token)obj;
//        if(t.ID==ID && t.Data )
//    }
}
