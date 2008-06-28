package org.argouml.language.csharp.importer;

import org.argouml.uml.reveng.ImportInterface;
import org.argouml.uml.reveng.ImportSettings;
import org.argouml.uml.reveng.FileImportUtils;
import org.argouml.uml.reveng.ImporterManager;
import org.argouml.language.csharp.importer.csparser.main.Lexer;
import org.argouml.language.csharp.importer.csparser.main.Parser;
import org.argouml.language.csharp.importer.csparser.collections.TokenCollection;
import org.argouml.language.csharp.importer.csparser.structural.CompilationUnitNode;

import org.argouml.kernel.Project;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.i18n.Translator;
import org.argouml.util.SuffixFilter;
import org.argouml.util.FileFilters;
import org.argouml.model.Model;

import org.apache.log4j.Logger;

import java.util.*;
import java.io.*;
// $Id: JavaImport.java 13667 2007-10-11 04:58:45Z tfmorris $
// Copyright (c) 1996-2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * This is the main class for C# reverse engineering.
 *
 * @author Thilina Hasantha <thilina.hasantha@gmail.com>
 */
public class CSharpImport implements ImportInterface {

    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(CSharpImport.class);

    /**
     * New model elements that were added
     */
    private Collection newElements;

    private List parsedElements = new ArrayList();

    /*
    * @see org.argouml.uml.reveng.ImportInterface#parseFiles(org.argouml.kernel.Project, java.util.Collection, org.argouml.uml.reveng.ImportSettings, org.argouml.application.api.ProgressMonitor)
    */
    public Collection parseFiles(Project p, Collection files,
                                 ImportSettings settings, ProgressMonitor monitor)
            throws ImportException {

        newElements = new HashSet();
        parsedElements = new ArrayList();
        monitor.updateMainTask(Translator.localize("dialog.import.pass1"));

        monitor.setMaximumProgress(files.size() * 3);
        doImportPass(p, files, settings, monitor, 0, 0);
        parseElements(p, settings, monitor, files.size(), 1);
        monitor.close();
        return newElements;
    }


    private void doImportPass(Project p, Collection files,
                              ImportSettings settings, ProgressMonitor monitor, int startCount,
                              int pass) throws ImportException {

        int count = startCount;
        for (Iterator it = files.iterator(); it.hasNext();) {
            if (monitor.isCanceled()) {
                monitor.updateSubTask(
                        Translator.localize("dialog.import.cancelled"));
                return;
            }
            Object file = it.next();
            if (file instanceof File) {
                parseFile(p, (File) file, settings, pass);
                monitor.updateProgress(count++);
                monitor.updateSubTask(Translator.localize(
                        "dialog.import.parsingAction",
                        new Object[]{((File) file).getAbsolutePath()}));
            } else {
                throw new ImportException("Object isn't a file " + file);
            }
        }


    }


    private void parseElements(Project p,
                               ImportSettings settings, ProgressMonitor monitor, int startCount,
                               int pass) throws ImportException {

//        int count = startCount;
//        for (Object cu:parsedElements) {
//
//        }

        // Create a modeller for the parser
//        Modeller modeller = new Modeller(p.getModel(), settings, "");
//        modeller.   addPackage("com");

//        modeller.addClass("com.pkg1.TestClass1", CSharpConstants.ACC_PUBLIC,null,new ArrayList<String>(),"");
//        modeller.addClass("TestClass1", CSharpConstants.ACC_PUBLIC,null,new ArrayList<String>(),"");
//        ArrayList<String> a=new ArrayList<String>();
//        a.add("TestClass1");
//        modeller.addClass("com.pkg1.TestClass2", CSharpConstants.ACC_PUBLIC,"com.pkg1.TestClass1",new ArrayList<String>(),"");
//
        //modeller.addOperation(CSharpConstants.ACC_PUBLIC,"int","foo1",new ArrayList<ParameterDeclaration>(), "");
//        ParameterDeclaration pd=new ParameterDeclaration((short)0,"String","param1");
//        List<ParameterDeclaration> pdl=new ArrayList<ParameterDeclaration>();
//        pdl.add(pd);
//        modeller.addOperation((short)(CSharpConstants.ACC_PUBLIC +  CSharpConstants.ACC_STATIC)
//                ,"int","foo1",pdl,"");



//        short modifiers= CSharpConstants.ACC_PUBLIC;
//        Object pk=Model.getModelManagementFactory().buildPackage("com","com");
//        Model.getCoreHelper().setRoot(pk,true);
//        Model.getCoreHelper().setNamespace(pk,p.getModel());
//        Model.getCoreHelper().addOwnedElement(p.getModel(), pk);
//
//        Object mClass =Model.getCoreFactory().createClass();
//        Model.getCoreHelper().setName(mClass, "Test");
//        Model.getCoreHelper().setNamespace(mClass,pk );
//        Model.getCoreHelper().setAbstract(mClass,
//                (modifiers & CSharpConstants.ACC_ABSTRACT) > 0);
//        Model.getCoreHelper().setLeaf(mClass,
//                (modifiers & CSharpConstants.ACC_FINAL) > 0);
//        Model.getCoreHelper().setRoot(mClass, false);
//
//        Object mClass1 =Model.getCoreFactory().createClass();
//        Model.getCoreHelper().setName(mClass1, "TestGHJ");
//        Model.getCoreHelper().setNamespace(mClass1,pk );
//        Model.getCoreHelper().setAbstract(mClass1,
//                (modifiers & CSharpConstants.ACC_ABSTRACT) > 0);
//        Model.getCoreHelper().setLeaf(mClass1,
//                (modifiers & CSharpConstants.ACC_FINAL) > 0);
//        Model.getCoreHelper().setRoot(mClass, false);
//        Model.getCoreFactory().buildGeneralization(mClass1,mClass,"ddd");
//
//
//        newElements.add(pk);
//        newElements.add(mClass);
//        newElements.add(mClass1);

        CSModeller cm=new CSModeller(p,settings);
        cm.model(parsedElements,monitor,startCount); 
        newElements.addAll(cm.getNewElements());


        //newElements.addAll(modeller.getNewElements());

        //Model.getCoreFactory().
//        Object up=Model.getModelManagementFactory().buildPackage("pkg1","com.pkg1");
//        Model.getCoreHelper().setRoot(up,true);
//        Object uc= Model.getCoreFactory().buildClass("Test",up);
//        Model.getCoreHelper().setRoot(uc,false);
//
//        newElements.add(up);
//        newElements.add(uc);




//        Object package1 =
//                Model.getModelManagementFactory().buildPackage("test1", null);
//        Object package2 =
//                Model.getModelManagementFactory().buildPackage("test2", null);
//
//        UMLClassDiagram cDiag = new UMLClassDiagram(package2);
//        newElements.add(cDiag);


    }


    /**
     * Do a single import pass of a single file.
     *
     * @param p        the project
     * @param f        the source file
     * @param settings the user provided import settings
     * @param pass     current import pass - 0 = single pass, 1 = pass 1 of 2, 2 =
     *                 pass 2 of 2
     */
    private void parseFile(Project p, File f, ImportSettings settings, int pass)
            throws ImportException {


        try {
            BufferedInputStream bs = new BufferedInputStream(new FileInputStream(f));
            Lexer l = new Lexer(bs, f.getAbsolutePath());
            TokenCollection toks = l.Lex();
            Parser px = new Parser();
            CompilationUnitNode cu = px.Parse(toks, l.StringLiterals);
            parsedElements.add(cu);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new ImportException("Error parsing file: " + f.getAbsolutePath());
        }

    }

    private String buildErrorString(File f) {
        String path = "";
        try {
            path = f.getCanonicalPath();
        } catch (IOException e) {
            // Just ignore - we'll use the simple file name
        }
        return "Exception in file: " + path + " " + f.getName();
    }


//    public static final SuffixFilter CS_FILE_FILTER = new
//            SuffixFilter("cs", "CSharp");

    public static final SuffixFilter CS_FILE_FILTER = new
        SuffixFilter("cs", "CSharp");
    /*
     * @see org.argouml.uml.reveng.ImportInterface#getSuffixFilters()
     */
    public SuffixFilter[] getSuffixFilters() {
        SuffixFilter[] result = {CS_FILE_FILTER};
        return result;
    }

    /*
     * @see org.argouml.uml.reveng.ImportInterface#isParseable(java.io.File)
     */
    public boolean isParseable(File file) {
        return FileImportUtils.matchesSuffix(file, getSuffixFilters());
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getName()
     */
    public String getName() {
        return " CSharp";
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    public String getInfo(int type) {
        switch (type) {
            case DESCRIPTION:
                return "This is a module for import from Java files.";
            case AUTHOR:
                return "Marcus Andersson, Thomas Neustupny, Andreas Rückert";
            case VERSION:
                return "1.0";
            default:
                return null;
        }
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        // We are permanently enabled
        return false;
    }

    /*
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        init();
        return true;
    }

    /**
     * Enable the importer.
     */
    public void init() {
        ImporterManager.getInstance().addImporter(this);
    }

    /*
    * @see org.argouml.uml.reveng.ImportInterface#getImportSettings()
    */
    public List getImportSettings() {
        return null;
    }

}
