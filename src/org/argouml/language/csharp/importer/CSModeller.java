package org.argouml.language.csharp.importer;

import org.argouml.kernel.Project;
import org.argouml.uml.reveng.ImportSettings;
import org.argouml.language.csharp.importer.csparser.structural.CompilationUnitNode;
import org.argouml.language.csharp.importer.csparser.structural.NamespaceNode;
import org.argouml.language.csharp.importer.csparser.structural.UsingDirectiveNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.types.ClassNode;
import org.argouml.language.csharp.importer.csparser.types.InterfaceNode;
import org.argouml.language.csharp.importer.csparser.members.MethodNode;
import org.argouml.language.csharp.importer.csparser.members.ParamDeclNode;
import org.argouml.language.csharp.importer.csparser.members.FieldNode;
import org.argouml.language.csharp.importer.csparser.members.InterfaceMethodNode;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;
import org.argouml.language.csharp.importer.bridge.ModifierMap;
import org.argouml.model.Model;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.i18n.Translator;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 22, 2008
 * Time: 9:33:34 PM
 */
public class CSModeller {
    Project p;
    ImportSettings settings;
    List elements = new ArrayList();
    CompilationUnitNode cu = null;
    int phase = 0;

    private static String TAG_CLASS = "cls#";
    private static String TAG_INTERFACE = "int#";
    private static String TAG_NS = "ns#";
    private static String TAG_GEN = "gen#";
    private static String TAG_EXTEND = "ext#";
    private static String TAG_OP = "opr#";
    private Object model;
    Hashtable ele = new Hashtable();


    private boolean noAssociations;
    private boolean arraysAsDatatype;


    public CSModeller(Project p, ImportSettings settings) {
        this.p = p;
        model = p.getModel();
        this.settings = settings;
        noAssociations = settings.isAttributeSelected();
        arraysAsDatatype = settings.isDatatypeSelected();
    }


    public void model(List cNodes, ProgressMonitor monitor, int startCount) {

        int count=startCount;
        phase = 0;
        for (Object obj : cNodes) {
            if (monitor.isCanceled()) {
                monitor.updateSubTask(
                        Translator.localize("dialog.import.cancelled"));
                return;
            }
            cu = (CompilationUnitNode) obj;
            addNamespace(cu.DefaultNamespace);
            addNamespaceNodes(cu.Namespaces);
            monitor.updateProgress(count++);
        }
        phase++;
        for (Object obj : cNodes) {
            if (monitor.isCanceled()) {
                monitor.updateSubTask(
                        Translator.localize("dialog.import.cancelled"));
                return;
            }
            cu = (CompilationUnitNode) obj;
            addNamespace(cu.DefaultNamespace);
            addNamespaceNodes(cu.Namespaces);
            monitor.updateProgress(count++);
        }
    }

    public void addNamespaceNodes(NodeCollection<NamespaceNode> nss) {
        for (NamespaceNode ns : nss) {
            if (phase == 0) {
                addNamespace(ns);
            }
            addNamespaceClasses(ns);

        }
        for (NamespaceNode ns : nss) {
            addNamespaceNodes(ns.Namespaces);
        }
    }

    private void addNamespaceClasses(NamespaceNode ns) {
        String parent = buildToParent(ns.Name.Identifier, ns.Name.Identifier.length);
        for (ClassNode cn : ns.Classes) {
            if (phase == 0) {
                addClass(cn.Modifiers, cn.Name.Identifier[cn.Name.Identifier.length - 1], parent);
            } else if (phase == 1) {
                addAttributes(cn, parent);
                addMethods(cn, parent);
                buildGeneralization(cn, ns);
            }
        }
        for (InterfaceNode cn : ns.Interfaces) {
            if (phase == 0) {
                addInterface(cn.Modifiers, cn.Name.Identifier[cn.Name.Identifier.length - 1], parent);
            } else if (phase == 1) {
                addMethods(cn, parent);
            }
        }
    }

    private void addMethods(ClassNode cn, String cPackage) {
        for (MethodNode mn : cn.Methods) {
            addOperation(cn.Name.Identifier[0], mn, cPackage);
        }
    }

    private void addMethods(InterfaceNode cn, String cPackage) {
        for (InterfaceMethodNode mn : cn.Methods) {
            addOperation(cn.Name.Identifier[0], mn, cPackage);
        }
    }

    private void addRootNamesapce(String name) {
        if (ele.get(TAG_NS + name) != null) {
            return;
        }
        Object pk = Model.getModelManagementFactory().buildPackage(name, name);
        Model.getCoreHelper().setRoot(pk, true);
        Model.getCoreHelper().setNamespace(pk, model);
        Model.getCoreHelper().addOwnedElement(model, pk);
        ele.put(TAG_NS + name, pk);
    }

    private void addSubNamesapce(String name, String parent) {
        if (ele.get(TAG_NS + parent + "." + name) != null) {
            return;
        }
        Object pk = Model.getModelManagementFactory().buildPackage(name, parent + "." + name);
        Model.getCoreHelper().setRoot(pk, true);
        Model.getCoreHelper().setNamespace(pk, ele.get(TAG_NS + parent));
        ele.put(TAG_NS + parent + "." + name, pk);
    }

    public void addNamespace(NamespaceNode ns) {
        for (int i = 0; i < ns.Name.Identifier.length; i++) {
            if (i == 0) {
                addRootNamesapce(ns.Name.Identifier[i]);
            } else {
                addSubNamesapce(ns.Name.Identifier[i], buildToParent(ns.Name.Identifier, i));
            }
        }
    }

    private String buildToParent(String[] sa, int k) {
        String p = "";
        for (int i = 0; i < k; i++) {
            if (i < k - 1) {
                p += sa[i] + ".";
            } else {
                p += sa[i];
            }
        }
        return p;
    }

    public Object addClass(long modifiers, String name, String parent) {
        if (ele.get(TAG_CLASS + parent + "." + name) != null) {
            return ele.get(TAG_CLASS + parent + "." + name);
        }
        short cmod = ModifierMap.getUmlModifierForVisibility(modifiers);
        //concatModifires(modifiers);
        Object mClass = Model.getCoreFactory().createClass();
        Model.getCoreHelper().setName(mClass, name);
        Model.getCoreHelper().setNamespace(mClass, ele.get(TAG_NS + parent));
        setVisibility(mClass, cmod);
        Model.getCoreHelper().setAbstract(mClass,
                (cmod & CSharpConstants.ACC_ABSTRACT) > 0);
        Model.getCoreHelper().setLeaf(mClass,
                (cmod & CSharpConstants.ACC_FINAL) > 0);
        Model.getCoreHelper().setRoot(mClass, false);
        ele.put(TAG_CLASS + parent + "." + name, mClass);
        return mClass;
    }

    public Object addInterface(long modifiers, String name, String parent) {
        if (ele.get(TAG_INTERFACE + parent + "." + name) != null) {
            return ele.get(TAG_INTERFACE + parent + "." + name);
        }
        short cmod = ModifierMap.getUmlModifierForVisibility(modifiers);
        //concatModifires(modifiers);
        Object mInterface = Model.getCoreFactory().createInterface();
        Model.getCoreHelper().setName(mInterface, name);
        Model.getCoreHelper().setNamespace(mInterface, ele.get(TAG_NS + parent));
        setVisibility(mInterface, cmod);

        Model.getCoreHelper().setRoot(mInterface, false);
        ele.put(TAG_INTERFACE + parent + "." + name, mInterface);
        return mInterface;
    }

    public void addOperation(String parent, MethodNode mn, String cPackage) {

        String name = mn.names.get(0).Identifier[0];
        String className = cPackage + "." + parent;
        String id = TAG_OP + className + "." + name + getParameterTypeString(mn);

        if (ele.get(id) != null) {
            return;
        }

        short cmod = ModifierMap.getUmlModifierForVisibility(mn.modifiers);
        Object cls = ele.get(TAG_CLASS + className);

        //return

        Object classifier = null;
        //check in classes
        String temp = buildToParent(mn.type.Identifier.Identifier, mn.type.Identifier.Identifier.length);
        classifier = getStoredDataType(temp, cPackage);

        Object mOperation = Model.getCoreFactory().buildOperation2(cls, classifier, name);
        setVisibility(mOperation, cmod);
        Model.getCoreHelper().setAbstract(mOperation,
                (cmod & CSharpConstants.ACC_ABSTRACT) > 0);
        Model.getCoreHelper().setLeaf(mOperation,
                (cmod & CSharpConstants.ACC_FINAL) > 0);
        Model.getCoreHelper().setRoot(mOperation, false);
        Model.getCoreHelper().setStatic(mOperation, (cmod & CSharpConstants.ACC_STATIC) > 0);


        Object parameter = null;
        if (mn.params != null) {
            for (ParamDeclNode p : mn.params) {

                classifier = null;
                //check in classes
//                classifier = getClasesByName(buildToParent(p.type.Identifier.Identifier,
//                        p.type.Identifier.Identifier.length), cPackage);
//                if (classifier == null) {
//                    classifier = getInterfaceByName(buildToParent(p.type.Identifier.Identifier,
//                            p.type.Identifier.Identifier.length), cPackage);
//                }
//                if (classifier == null) {
//                    classifier = Model.getCoreFactory().buildClass(buildToParent(p.type.Identifier.Identifier,
//                            p.type.Identifier.Identifier.length), ele.get(TAG_NS + cPackage));
//                }
                classifier=getStoredDataType(buildToParent(p.type.Identifier.Identifier,
                        p.type.Identifier.Identifier.length),cPackage);
                parameter =
                        Model.getCoreFactory().buildParameter(mOperation, classifier);
                Model.getCoreHelper().setName(parameter, p.name);

            }
        }

        ele.put(id, mOperation);


    }


    public void addOperation(String parent, InterfaceMethodNode mn, String cPackage) {

            String name = mn.names.get(0).Identifier[0];
            String className = cPackage + "." + parent;
            String id = TAG_OP + className + "." + name + getParameterTypeString(mn);

            if (ele.get(id) != null) {
                return;
            }

            short cmod = ModifierMap.getUmlModifierForVisibility(mn.modifiers);
            Object cls = ele.get(TAG_CLASS + className);
            if(cls==null){
                cls = ele.get(TAG_INTERFACE + className);
            }
            if(cls==null){
                return;
            }
            //return

            Object classifier = null;
            //check in classes
            String temp = buildToParent(mn.type.Identifier.Identifier, mn.type.Identifier.Identifier.length);
            classifier = getStoredDataType(temp, cPackage);

            Object mOperation = Model.getCoreFactory().buildOperation2(cls, classifier, name);
            setVisibility(mOperation, cmod);
            Model.getCoreHelper().setAbstract(mOperation,
                    (cmod & CSharpConstants.ACC_ABSTRACT) > 0);
            Model.getCoreHelper().setLeaf(mOperation,
                    (cmod & CSharpConstants.ACC_FINAL) > 0);
            Model.getCoreHelper().setRoot(mOperation, false);
            Model.getCoreHelper().setStatic(mOperation, (cmod & CSharpConstants.ACC_STATIC) > 0);


            Object parameter = null;
            if (mn.params != null) {
                for (ParamDeclNode p : mn.params) {

                    classifier = null;
                    //check in classes
//                classifier = getClasesByName(buildToParent(p.type.Identifier.Identifier,
//                        p.type.Identifier.Identifier.length), cPackage);
//                if (classifier == null) {
//                    classifier = getInterfaceByName(buildToParent(p.type.Identifier.Identifier,
//                            p.type.Identifier.Identifier.length), cPackage);
//                }
//                if (classifier == null) {
//                    classifier = Model.getCoreFactory().buildClass(buildToParent(p.type.Identifier.Identifier,
//                            p.type.Identifier.Identifier.length), ele.get(TAG_NS + cPackage));
//                }
                    classifier=getStoredDataType(buildToParent(p.type.Identifier.Identifier,
                            p.type.Identifier.Identifier.length),cPackage);
                    parameter =
                            Model.getCoreFactory().buildParameter(mOperation, classifier);
                    Model.getCoreHelper().setName(parameter, p.name);

                }
            }

            ele.put(id, mOperation);


        }



    void addAttributes(ClassNode cn, String cPackage) {
        if (cn.Fields != null)
            for (FieldNode f : cn.Fields) {
                addAttribute(cn, f, cPackage);
            }
    }


    void addAttribute(ClassNode cn, FieldNode fn, String cPackage) {

        short modifiers = ModifierMap.getUmlModifierForVisibility(fn.modifiers);
        String typeSpec = buildToParent(fn.type.Identifier.Identifier, fn.type.Identifier.Identifier.length);
        String name = buildToParent(fn.names.get(0).Identifier, fn.names.get(0).Identifier.length);
        String initializer = null;
        String docs = "";
        boolean forceIt = false;


        String multiplicity = "1_1";
        Object mClassifier = null;
        String className = cPackage + "." + cn.Name.Identifier[0];
        Object cls = ele.get(TAG_CLASS + className);

        if (typeSpec != null) {
            if (!arraysAsDatatype && typeSpec.indexOf('[') != -1) {
                typeSpec = typeSpec.substring(0, typeSpec.indexOf('['));
                multiplicity = "1_N";
            }
            mClassifier = getStoredDataType(typeSpec, cPackage);
        }

        // if we want to create a UML attribute:
        if (noAssociations) {
            Object mAttribute = buildAttribute(cls, mClassifier, name);
            setOwnerScope(mAttribute, modifiers);
            setVisibility(mAttribute, modifiers);
            Model.getCoreHelper().setMultiplicity(mAttribute, multiplicity);

//            if (Model.getFacade().isAClassifier(mClassifier)) {
//                // TODO: This should already have been done in buildAttribute
//                Model.getCoreHelper().setType(mAttribute, mClassifier);
//            } else {
//                // the type resolution failed to find a valid classifier.
//                logError("Modeller.java: a valid type for a parameter "
//                        + "could not be resolved:\n "
//                        + "In file: " + fileName + ", for attribute: ",
//                        Model.getFacade().getName(mAttribute));
//            }

            // Set the initial value for the attribute.
            if (initializer != null) {

                // we must remove line endings and tabs from the intializer
                // strings, otherwise the classes will display horribly.
                initializer = initializer.replace('\n', ' ');
                initializer = initializer.replace('\t', ' ');

                Object newInitialValue =
                        Model.getDataTypesFactory()
                                .createExpression("CSharp",
                                        initializer);
                Model.getCoreHelper().setInitialValue(
                        mAttribute,
                        newInitialValue);
            }

            if ((modifiers & CSharpConstants.ACC_FINAL) > 0) {
                Model.getCoreHelper().setReadOnly(mAttribute, true);
            } else if (Model.getFacade().isReadOnly(mAttribute)) {
                Model.getCoreHelper().setReadOnly(mAttribute, true);
            }
            //addDocumentationTag(mAttribute, javadoc);
        }
        // we want to create a UML association from the java attribute
//        else {
//
//            Object mAssociationEnd = getAssociationEnd(name, mClassifier);
//            setTargetScope(mAssociationEnd, modifiers);
//            setVisibility(mAssociationEnd, modifiers);
//            Model.getCoreHelper().setMultiplicity(mAssociationEnd, multiplicity);
//            Model.getCoreHelper().setType(mAssociationEnd, mClassifier);
//            Model.getCoreHelper().setName(mAssociationEnd, name);
//            if ((modifiers & CSharpConstants.ACC_FINAL) > 0) {
//                Model.getCoreHelper().setReadOnly(mAssociationEnd, true);
//            }
//            if (!mClassifier.equals(parseState.getClassifier())) {
//                // Because if they are equal,
//                // then getAssociationEnd(name, mClassifier) could return
//                // the wrong assoc end, on the other hand the navigability
//                // is already set correctly (at least in this case), so the
//                // next line is not necessary. (maybe never necessary?) - thn
//                Model.getCoreHelper().setNavigable(mAssociationEnd, true);
//            }
////            addDocumentationTag(mAssociationEnd, javadoc);
//        }
    }


    private Object buildReturnParameter(Object operation, Object classifier) {
        Object parameter = buildParameter(operation, classifier, "return");
        Model.getCoreHelper().setKind(parameter, Model.getDirectionKind().getReturnParameter());
        return parameter;
    }

    private Object buildParameter(Object operation, Object classifier,
                                  String name) {
        Object parameter =
                Model.getCoreFactory().buildParameter(operation, classifier);
        Model.getCoreHelper().setName(parameter, name);
        return parameter;
    }

    private void buildGeneralization(ClassNode cn, NamespaceNode ns) {
        if (cn.BaseClasses != null) {
            for (TypeNode tn : cn.BaseClasses) {
                String parent = buildToParent(tn.Identifier.Identifier, tn.Identifier.Identifier.length);
                String child = buildToParent(cn.Name.Identifier, cn.Name.Identifier.length);
                String pkg = buildToParent(ns.Name.Identifier, ns.Name.Identifier.length);
                Object c = getStoredDataType(child, pkg);
                Object p = getStoredDataType(parent, pkg);
                Object n=getNameSpace(pkg);
                Object g=null;
                if(Model.getFacade().isAInterface(p)){
                    g=buildRealization(c,p,n);
                }else{
                    g=buildGeneralizations(c, p);
                }
                if(g!=null)
                    Model.getCoreHelper().setName(g,child +" -> "+parent); 

            }
        }
    }


    private Object buildGeneralizations(Object child, Object parnt) {
        Object gen= Model.getCoreFactory().buildGeneralization(child, parnt);
        return gen;
    }

    private Object buildRealization(Object child, Object parnt, Object namespace) {
        Object rel= Model.getCoreFactory().buildRealization(child,parnt,namespace);
        return rel;
    }


    Object getClasesByName(String paramType, String cPackage) {
        Object kx = null;
        if (paramType.contains(".")) {
            return ele.get(TAG_CLASS + paramType);
        } else {
            for (UsingDirectiveNode u : cu.UsingDirectives) {
                kx = ele.get(TAG_CLASS + buildToParent(u.Target.Identifier, u.Target.Identifier.length)
                        + paramType);
                if (kx != null) {
                    return kx;
                }
            }
            return null;
        }
    }

    Object getInterfaceByName(String paramType, String cPackage) {
        Object kx = null;
        if (paramType.contains(".")) {
            return ele.get(TAG_INTERFACE + paramType);
        } else {
            for (UsingDirectiveNode u : cu.UsingDirectives) {
                kx = ele.get(TAG_INTERFACE + buildToParent(u.Target.Identifier, u.Target.Identifier.length)
                        + paramType);
                if (kx != null) {
                    return kx;
                }
            }
            return null;
        }
    }

    /**
     * Set the visibility for a model element.
     *
     * @param element   The model element.
     * @param modifiers A sequence of modifiers which may contain
     *                  'private', 'protected' or 'public'.
     */
    private void setVisibility(Object element, short modifiers) {
        if ((modifiers & CSharpConstants.ACC_PRIVATE) > 0) {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getPrivate());
        } else if ((modifiers & CSharpConstants.ACC_PROTECTED) > 0) {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getProtected());
        } else if ((modifiers & CSharpConstants.ACC_PUBLIC) > 0) {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getPublic());
        } else {
            Model.getCoreHelper().setVisibility(
                    element,
                    Model.getVisibilityKind().getPackage());
        }
    }


    private void setOwnerScope(Object feature, short modifiers) {
        Model.getCoreHelper().setStatic(
                feature, (modifiers & CSharpConstants.ACC_STATIC) > 0);
    }

    private String getParameterTypeString(MethodNode mn) {
        String k = "";
        if (mn.params == null) {
            return k;
        }
        for (ParamDeclNode p : mn.params) {
            k += "|" + buildToParent(p.type.Identifier.Identifier,
                    p.type.Identifier.Identifier.length);
        }
        return k.toLowerCase();
    }

    private String getParameterTypeString(InterfaceMethodNode mn) {
        String k = "";
        if (mn.params == null) {
            return k;
        }
        for (ParamDeclNode p : mn.params) {
            k += "|" + buildToParent(p.type.Identifier.Identifier,
                    p.type.Identifier.Identifier.length);
        }
        return k.toLowerCase();
    }


    private short concatModifires(short[] modifiers) {
        short mod = 0;
        for (int i = 0; i < modifiers.length; i++) {
            mod = (short) (mod + modifiers[i]);
        }
        return mod;
    }

    public Collection getNewElements() {
        return ele.values();
    }

    public Object getStoredDataType(String name, String cPackage) {

        Object classifier = getClasesByName(name, cPackage);
        if (classifier == null) {
            classifier = getInterfaceByName(name, cPackage);
        }
        if (classifier == null) {
            classifier = addClass(0, name, cPackage);
            ele.put(TAG_CLASS+cPackage+"."+name,classifier);
        }
        return classifier;
    }

    public Object getNameSpace(String pkg){
        return ele.get(TAG_NS+pkg);
    }

    private Object buildAttribute(Object classifier, Object type, String name) {
        Object mAttribute =
                Model.getCoreFactory().buildAttribute2(classifier, type);
        Model.getCoreHelper().setName(mAttribute, name);
        return mAttribute;
    }
}
