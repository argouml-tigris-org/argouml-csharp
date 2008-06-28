package org.argouml.language.csharp.importer.csparser.main;

import org.argouml.language.csharp.importer.csparser.structural.CompilationUnitNode;
import org.argouml.language.csharp.importer.csparser.collections.TokenCollection;
import org.argouml.uml.reveng.ImportInterface;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 5:12:45 PM
 */
public class Test {
    public static void main(String[] args) throws ImportInterface.ImportException {
        try {
            parseFile();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void parseFile() throws IOException, ImportInterface.ImportException {
        //eventquery.cs
//        BufferedInputStream bs=new BufferedInputStream(new FileInputStream("test/Parser.cs"));
//        BufferedInputStream bs=new BufferedInputStream(new FileInputStream("test/Bill.cs"));
        BufferedInputStream bs=new BufferedInputStream(new FileInputStream("test/eventquery.cs"));
        Lexer l = new Lexer(bs,"");
        TokenCollection toks = l.Lex();
//        for(int i=0;i<l.StringLiterals.size();i++){
//            System.out.println(l.StringLiterals.get(i));
//        }
        Parser p = new Parser();
        CompilationUnitNode cu = p.Parse(toks, l.StringLiterals);
        StringBuilder sb=new StringBuilder();
        cu.ToSource(sb);
        //System.out.println(sb.toString());
    }
}
