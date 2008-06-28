package org.argouml.language.csharp.importer.csparser.main;

import org.argouml.language.csharp.importer.csparser.enums.TokenID;
import org.argouml.language.csharp.importer.csparser.enums.Modifier;
import org.argouml.language.csharp.importer.csparser.enums.PreprocessorID;
import org.argouml.language.csharp.importer.csparser.enums.IntegralType;
import org.argouml.language.csharp.importer.csparser.structural.*;
import org.argouml.language.csharp.importer.csparser.collections.TokenCollection;
import org.argouml.language.csharp.importer.csparser.collections.ParseStateCollection;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.collections.ExpressionList;
import org.argouml.language.csharp.importer.csparser.types.*;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.*;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.primitive.*;
import org.argouml.language.csharp.importer.csparser.preprocessornodes.PPNode;
import org.argouml.language.csharp.importer.csparser.preprocessornodes.PPDefineNode;
import org.argouml.language.csharp.importer.csparser.preprocessornodes.PPEndIfNode;
import org.argouml.language.csharp.importer.csparser.members.*;
import org.argouml.language.csharp.importer.csparser.statements.*;
import org.argouml.language.csharp.importer.csparser.interfaces.IType;
import org.argouml.language.csharp.importer.csparser.interfaces.IMemberAccessible;

import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 2:31:45 PM
 */
public class Parser{
		public static Token EOF = new Token(org.argouml.language.csharp.importer.csparser.enums.TokenID.Eof);
		private static Hashtable<Integer, Long> modMap;
		private static Hashtable<String, Byte> preprocessor;
		private static Hashtable<String, Byte> ppDefs = new Hashtable<String,Byte>();
		private static int[] precedence;

		private CompilationUnitNode cu;
		private TokenCollection tokens;
		private List<String> strings;

		private Stack<NamespaceNode> namespaceStack;
		private Stack<ClassNode> typeStack;
		private org.argouml.language.csharp.importer.csparser.collections.Stack<ExpressionNode> exprStack;
		public ParseStateCollection CurrentState;
		private InterfaceNode curInterface;
		private Token curtok;
		private long curmods;
		private NodeCollection<AttributeNode> curAttributes;
		private int index = 0;
		private boolean isLocalConst = false;
		private int lineCount = 1;
		private boolean ppCondition = false;
		private boolean inPPDirective = false;




		public Parser()
		{
            modMap = new Hashtable<Integer, Long>();
			modMap.put(TokenID.New, Modifier.New);
			modMap.put(TokenID.Public, Modifier.Public);
            modMap.put(TokenID.Partial, Modifier.Partial);            
            modMap.put(TokenID.Protected, Modifier.Protected);
			modMap.put(TokenID.Internal, Modifier.Internal);
			modMap.put(TokenID.Private, Modifier.Private);
			modMap.put(TokenID.Abstract, Modifier.Abstract);
			modMap.put(TokenID.Sealed, Modifier.Sealed);
			modMap.put(TokenID.Static, Modifier.Static);
			modMap.put(TokenID.Virtual, Modifier.Virtual);
			modMap.put(TokenID.Override, Modifier.Override);
			modMap.put(TokenID.Extern, Modifier.Extern);
			modMap.put(TokenID.Readonly, Modifier.Readonly);
			modMap.put(TokenID.Volatile, Modifier.Volatile);
			modMap.put(TokenID.Ref, Modifier.Ref);
			modMap.put(TokenID.Out, Modifier.Out);
			modMap.put(TokenID.Assembly, Modifier.Assembly);
			modMap.put(TokenID.Field, Modifier.Field);
			modMap.put(TokenID.Event, Modifier.Event);
			modMap.put(TokenID.Method, Modifier.Method);
			modMap.put(TokenID.Param, Modifier.Param);
			modMap.put(TokenID.Property, Modifier.Property);
			modMap.put(TokenID.Return, Modifier.Return);
			modMap.put(TokenID.Type, Modifier.Type);

			// all default to zero
			precedence = new int[0xFF];

			// these start at 80 for no paticular reason
			precedence[ (int)TokenID.LBracket]		= 0x90;

			precedence[ (int)TokenID.LParen]		= 0x80;
			precedence[ (int)TokenID.Star ]		 	= 0x7F;
			precedence[ (int)TokenID.Slash ]	 	= 0x7F;
			precedence[ (int)TokenID.Percent ]	 	= 0x7F;
			precedence[ (int)TokenID.Plus ]		 	= 0x7E;
			precedence[ (int)TokenID.Minus ]	 	= 0x7E;
			precedence[ (int)TokenID.ShiftLeft ] 	= 0x7D;
			precedence[ (int)TokenID.ShiftRight] 	= 0x7D;
			precedence[ (int)TokenID.Less ]		 	= 0x7C;
			precedence[ (int)TokenID.Greater ]	 	= 0x7C;
			precedence[ (int)TokenID.LessEqual ] 	= 0x7C;
			precedence[ (int)TokenID.GreaterEqual ]	= 0x7C;
			precedence[ (int)TokenID.EqualEqual ]	= 0x7B;
			precedence[ (int)TokenID.NotEqual ]	 	= 0x7B;
			precedence[ (int)TokenID.BAnd ]		 	= 0x7A;
			precedence[ (int)TokenID.BXor ]		 	= 0x79;
			precedence[ (int)TokenID.BOr ]		 	= 0x78;
			precedence[ (int)TokenID.And]			= 0x77;
			precedence[ (int)TokenID.Or]			= 0x76;


			preprocessor = new Hashtable<String, Byte>();

			preprocessor.put("define", PreprocessorID.Define);
			preprocessor.put("undef", PreprocessorID.Undef);
			preprocessor.put("if", PreprocessorID.If);
			preprocessor.put("elif", PreprocessorID.Elif);
			preprocessor.put("else", PreprocessorID.Else);
			preprocessor.put("endif", PreprocessorID.Endif);
			preprocessor.put("line", PreprocessorID.Line);
			preprocessor.put("error", PreprocessorID.Error);
			preprocessor.put("warning", PreprocessorID.Warning);
			preprocessor.put("region", PreprocessorID.Region);
			preprocessor.put("endregion", PreprocessorID.Endregion);
			preprocessor.put("pragma", PreprocessorID.Pragma);
        }

		public CompilationUnitNode Parse(TokenCollection tokens, List<String> strings)
		{
			this.tokens = tokens;
			this.strings = strings;
			curmods = Modifier.Empty;
			curAttributes = new NodeCollection<AttributeNode>();

			CurrentState = new ParseStateCollection();

			cu = new CompilationUnitNode();
			namespaceStack = new Stack<NamespaceNode>();
			namespaceStack.push(cu.DefaultNamespace);
			typeStack = new Stack<ClassNode>();

			exprStack = new org.argouml.language.csharp.importer.csparser.collections.Stack<ExpressionNode>();

			// begin parse
			Advance();
			ParseNamespaceOrTypes();

			return cu;
		}

		private void ParseNamespaceOrTypes()
		{
			while(!curtok.equals(EOF))
			{
				// todo: account for assembly Attributes
				ParsePossibleAttributes(true);
				if (curAttributes.size() > 0)
				{
					for (AttributeNode an : curAttributes)
					{
						cu.Attributes.add(an);
					}
					curAttributes.clear();
				}

				// can be usingDirectives, globalAttribs, or NamespaceMembersDecls
				// NamespaceMembersDecls include namespaces, class, struct, interface, enum, delegate
				switch (curtok.ID)
				{
					case TokenID.Using:
						// using directive
						ParseUsingDirectives();
						break;

					case TokenID.New:
					case TokenID.Public:
					case TokenID.Protected:
                    case TokenID.Partial:
                    case TokenID.Static:
                    case TokenID.Internal:
					case TokenID.Private:
					case TokenID.Abstract:
					case TokenID.Sealed:
						//parseTypeModifier();
						curmods |= modMap.get(curtok.ID);
						Advance();
						break;

					case TokenID.Namespace:
						ParseNamespace();
						break;

					case TokenID.Class:
						ParseClass();
						break;

					case TokenID.Struct:
						ParseStruct();
						break;

					case TokenID.Interface:
						ParseInterface();
						break;

					case TokenID.Enum:
						ParseEnum();
						break;

					case TokenID.Delegate:
						ParseDelegate();
						break;

					case TokenID.Semi:
						Advance();
						break;

					default:
						return;
				}
			}
		}
		private void ParseUsingDirectives()
		{
			do
			{
				Advance();
				UsingDirectiveNode node = new UsingDirectiveNode();

				IdentifierExpression nameOrAlias = ParseQualifiedIdentifier();
				if (curtok.ID == TokenID.Equal)
				{
					Advance();
					IdentifierExpression target = ParseQualifiedIdentifier();
					node.setAliasName(nameOrAlias);
					node.Target = target;
				}
				else
				{
					node.Target = nameOrAlias;
				}
				AssertAndAdvance(TokenID.Semi);

				cu.UsingDirectives.add(node);

			} while (curtok.ID == TokenID.Using);
		}
		private PPNode ParsePreprocessorDirective()
		{
			PPNode result = null;
			int startLine = lineCount;

			inPPDirective = true;
			Advance(); // over hash

			IdentifierExpression ie = ParseIdentifierOrKeyword();
			String ppKind = ie.Identifier[0];

			byte id = PreprocessorID.Empty;
			if (preprocessor.containsKey(ppKind))
			{
				id = preprocessor.get(ppKind);
			}
			else
			{
				ReportError("Preprocessor directive must be valid identifier, rather than \"" + ppKind + "\".");
			}

			switch (id)
			{
				case PreprocessorID.Define:
					// conditional-symbol pp-newline
					IdentifierExpression def = ParseIdentifierOrKeyword();
					if (!ppDefs.containsKey(def.Identifier[0]))
					{
						ppDefs.put(def.Identifier[0], PreprocessorID.Empty);
					}
					result = new PPDefineNode(def);
					break;
				case PreprocessorID.Undef:
					// conditional-symbol pp-newline
					IdentifierExpression undef = ParseIdentifierOrKeyword();
					if(ppDefs.containsKey(undef.Identifier[0]))
					{
						ppDefs.remove(undef.Identifier[0]);
					}
					result = new PPDefineNode(undef);
					break;
				case PreprocessorID.If:
					// pp-expression pp-newline conditional-section(opt)
					if (curtok.ID == TokenID.LParen)
					{
						Advance();
					}
					int startCount = lineCount;
					ppCondition = false;

					// todo: account for true, false, ||, &&, ==, !=, !
					IdentifierExpression ifexpr = ParseIdentifierOrKeyword();
					if (ppDefs.containsKey(ifexpr.Identifier[0]))
					{
						ppCondition = true;
					}
					//result = new PPIfNode(ParseExpressionToNewline());
					if (curtok.ID == TokenID.RParen)
					{
						Advance();
					}
					if (ppCondition == false)
					{
						// skip this block
						SkipToElseOrEndIf();
					}
					break;
				case PreprocessorID.Elif:
					// pp-expression pp-newline conditional-section(opt)
					SkipToEOL(startLine);
					break;
				case PreprocessorID.Else:
					// pp-newline conditional-section(opt)
					if (ppCondition == true)
					{
						// skip this block
						SkipToElseOrEndIf();
					}
					break;
				case PreprocessorID.Endif:
					// pp-newline
					result = new PPEndIfNode();
					ppCondition = false;
					break;
				case PreprocessorID.Line:
					// line-indicator pp-newline
					SkipToEOL(startLine);
					break;
				case PreprocessorID.Error:
					// pp-message
					SkipToEOL(startLine);
					break;
				case PreprocessorID.Warning:
					// pp-message
					SkipToEOL(startLine);
					break;
				case PreprocessorID.Region:
					// pp-message
					SkipToEOL(startLine);
					break;
				case PreprocessorID.Endregion:
					// pp-message
					SkipToEOL(startLine);
					break;
				case PreprocessorID.Pragma:
					// pp-message
					SkipToEOL(startLine);
					break;
				default:
					break;
			}
			inPPDirective = false;
			return result;
		}
		private void ParsePossibleAttributes(boolean isGlobal)
		{
			while (curtok.ID == TokenID.LBracket)
			{

				Advance(); // advance over LBracket token
				curmods = ParseAttributeModifiers();

				if (isGlobal && curmods == Modifier.GlobalAttributeMods)
				{
					// nothing to check, globally positioned Attributes can still apply to namespaces etc
				}
				else
				{
					long attribMask = ~(Modifier.AttributeMods);
					if (((long)curmods & attribMask) != (long)Modifier.Empty)
						ReportError("Attribute contains illegal Modifiers.");
				}

				long curAttribMods = curmods;
				curmods = Modifier.Empty;

				if (curAttribMods != Modifier.Empty)
				{
					AssertAndAdvance(TokenID.Colon);
				}

				AttributeNode node = new AttributeNode();
				curAttributes.add(node);
				node.Modifiers = curAttribMods;

				while (curtok.ID != TokenID.RBracket && curtok.ID != TokenID.Eof)
				{
					node.Name = ParseQualifiedIdentifier();

					if (curtok.ID == TokenID.LParen)
					{
						// has attribute arguments
						Advance(); // over lparen

						// named args are ident = expr
						// positional args are just expr
						while (curtok.ID != TokenID.RParen && curtok.ID != TokenID.Eof)
						{
							AttributeArgumentNode aNode = new AttributeArgumentNode();

							if (tokens.size() > index + 2 &&
								curtok.ID == TokenID.Ident &&
								tokens.get(index).ID == TokenID.Equal)
							{
								// named argument
								aNode.ArgumentName = ParseQualifiedIdentifier();
								Advance(); // over '='
							}
							aNode.Expression = ParseExpression();
							node.Arguments.add(aNode);

							if (curtok.ID == TokenID.Comma)
							{
								Advance(); // over comma
							}
						}
						AssertAndAdvance(TokenID.RParen);  // over rparen
						if (tokens.size() > index + 2 &&
							curtok.ID == TokenID.Comma &&
							tokens.get(index).ID != TokenID.RBracket)
						{
							Advance(); // over comma
							node = new AttributeNode();
							curAttributes.add(node);
							node.Modifiers = curAttribMods;
						}
					}
					if (curtok.ID == TokenID.Comma)
					{
						// comma can hang a t end like enums
						Advance();
					}
				}
				AssertAndAdvance(TokenID.RBracket); // over rbracket
			}
		}
		private void ParseNamespace()
		{
			if (curmods != Modifier.Empty)
				ReportError("Namespace can not contain Modifiers");

			NamespaceNode node = new NamespaceNode();
			if (cu.Namespaces.size() == 1 && cu.Namespaces.get(0) == cu.DefaultNamespace)
			{
				cu.Namespaces.clear();
			}

			cu.Namespaces.add(node);
			namespaceStack.push(node);

			Advance(); // advance over Namespace token
			node.Name = ParseQualifiedIdentifier();

			AssertAndAdvance(TokenID.LCurly);

			ParseNamespaceOrTypes();

			AssertAndAdvance(TokenID.RCurly);
			namespaceStack.pop();
		}

		// types
		private void ParseClass()
		{
			long classMask = ~((long)Modifier.ClassMods);
			if ( ((long)curmods & classMask) != (long)Modifier.Empty)
				ReportError("Class contains illegal Modifiers.");

			ClassNode node = new ClassNode();
			typeStack.push(node);
			namespaceStack.peek().Classes.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			Advance(); // advance over Class token
			node.Name = ParseQualifiedIdentifier();

			if (curtok.ID == TokenID.Colon) // for base members
			{
				Advance();
				node.BaseClasses.add(ParseType());
				while (curtok.ID == TokenID.Comma)
				{
					Advance();
					node.BaseClasses.add(ParseType());
				}
			}
			AssertAndAdvance(TokenID.LCurly);

			while (curtok.ID != TokenID.RCurly) // guard for empty
			{
				ParseClassMember();
			}

			AssertAndAdvance(TokenID.RCurly);

			typeStack.pop();

		}
		private void ParseInterface()
		{

            InterfaceNode node = new InterfaceNode();
			namespaceStack.peek().Interfaces.add(node);
			curInterface = node;

			long interfaceMask = ~(long)Modifier.InterfaceMods;
			if (((long)curmods & interfaceMask) != (long)Modifier.Empty)
				ReportError("Interface contains illegal Modifiers");

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			Advance(); // advance over Interface token
			node.Name = ParseQualifiedIdentifier();

			if (curtok.ID == TokenID.Colon) // for base members
			{
				Advance();
				node.BaseClasses.add(ParseType());
				while (curtok.ID == TokenID.Comma)
				{
					Advance();
					node.BaseClasses.add(ParseType());
				}
			}
			AssertAndAdvance(TokenID.LCurly);

			while (curtok.ID != TokenID.RCurly) // guard for empty
			{
				ParseInterfaceMember();
			}

			AssertAndAdvance(TokenID.RCurly);

			curInterface = null;

		}
		private void ParseStruct()
		{
			StructNode node = new StructNode();
			typeStack.push(node);
			namespaceStack.peek().Structs.add(node);

			long structMask = ~(long)Modifier.StructMods;
			if (((long)curmods & structMask) != (long)Modifier.Empty)
				ReportError("Struct contains illegal Modifiers");

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			Advance(); // advance over Struct token
			node.Name = ParseQualifiedIdentifier();

			if (curtok.ID == TokenID.Colon) // for base members
			{
				Advance();
				node.BaseClasses.add(ParseType());
				while (curtok.ID == TokenID.Comma)
				{
					Advance();
					node.BaseClasses.add(ParseType());
				}
			}
			AssertAndAdvance(TokenID.LCurly);

			while (curtok.ID != TokenID.RCurly) // guard for empty
			{
				ParseClassMember();
			}

			AssertAndAdvance(TokenID.RCurly);

			typeStack.pop();
		}
		private void ParseEnum()
		{
			EnumNode node = new EnumNode();
			// todo: this needs to have any nested class info, or go in potential container class
			namespaceStack.peek().Enums.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			long enumMask = ~(long)Modifier.EnumMods;
			if (((long)curmods & enumMask) != (long)Modifier.Empty)
				ReportError("Enum contains illegal Modifiers");

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			Advance(); // advance over Enum token
			node.Name = ParseQualifiedIdentifier();

			if (curtok.ID == TokenID.Colon) // for base type
			{
				Advance();
				node.BaseClass = ParseType();
			}
			AssertAndAdvance(TokenID.LCurly);

			while (curtok.ID != TokenID.RCurly) // guard for empty
			{
				ParseEnumMember();
			}

			AssertAndAdvance(TokenID.RCurly);
			if (curtok.ID == TokenID.Semi)
			{
				Advance();
			}
		}
		private void ParseDelegate()
		{
			DelegateNode node = new DelegateNode();
			namespaceStack.peek().Delegates.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			long delegateMask = ~(long)Modifier.DelegateMods;
			if (((long)curmods & delegateMask) != (long)Modifier.Empty)
				ReportError("Delegate contains illegal Modifiers");

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			Advance(); // advance over delegate token
			node.Type = ParseType();
			node.Name = ParseQualifiedIdentifier();
			node.Params = ParseParamList();

			AssertAndAdvance(TokenID.Semi);
		}

		// members
		private void ParseClassMember()
		{
			// const field method property event indexer operator ctor ~ctor cctor typedecl
			ParsePossibleAttributes(false);
			ParseModifiers();
			switch (curtok.ID)
			{
				case TokenID.Class:
					ParseClass();
					break;

				case TokenID.Interface:
					ParseInterface();
					break;

				case TokenID.Struct:
					ParseStruct();
					break;

				case TokenID.Enum:
					ParseEnum();
					break;

				case TokenID.Delegate:
					ParseDelegate();
					break;

				case TokenID.Const:
					ParseConst();
					break;

				case TokenID.Event:
					ParseEvent();
					break;

				case TokenID.Tilde:
					ParseDestructor();
					break;

				case TokenID.Explicit:
				case TokenID.Implicit:
					ParseOperatorDecl(null);
					break;

				default:
					TypeNode type = ParseType();
					if (type == null)
					{
						ReportError("Expected type or ident in member definition");
					}
					switch (curtok.ID)
					{
						case TokenID.Operator:
							ParseOperatorDecl(type);
							break;
						case TokenID.LParen:
							ParseCtor(type);
							break;
						case TokenID.This: // can be iface.this too, see below
							ParseIndexer(type, null);
							break;
						default:
							IdentifierExpression name2 = ParseQualifiedIdentifier();
							if (name2 == null)
							{
								ReportError("Expected Name or ident in member definition");
							}
							switch (curtok.ID)
							{
								case TokenID.This:
									ParseIndexer(type, name2);
									break;
								case TokenID.Comma:
								case TokenID.Equal:
								case TokenID.Semi:
									ParseField(type, name2);
									break;
								case TokenID.LParen:
									ParseMethod(type, name2);
									break;
								case TokenID.LCurly:
									ParseProperty(type, name2);
									break;
								default:
									ReportError("Invalid member syntax");
									break;
							}
							break;
					}
					break;
			}
		}
		private void ParseInterfaceMember()
		{
			ParsePossibleAttributes(false);

			ParseModifiers();
			switch (curtok.ID)
			{
				case TokenID.Event:
					// event
					InterfaceEventNode node = new InterfaceEventNode();
					curInterface.Events.add(node);

					if (curAttributes.size() > 0)
					{
						node.Attributes = curAttributes;
						curAttributes = new NodeCollection<AttributeNode>();
					}

					node.Modifiers = curmods;
					curmods = Modifier.Empty;
					AssertAndAdvance(TokenID.Event);
					node.Type = ParseType();
					node.Names.add(ParseQualifiedIdentifier());
					AssertAndAdvance(TokenID.Semi);

					break;
				default:
					TypeNode type = ParseType();
					if (type == null)
					{
						ReportError("Expected type or ident in interface member definition.");
					}
					switch (curtok.ID)
					{
						case TokenID.This:
							// interface indexer
							InterfaceIndexerNode iiNode = new InterfaceIndexerNode();
							if (curAttributes.size() > 0)
							{
								iiNode.Attributes = curAttributes;
								curAttributes = new NodeCollection<AttributeNode>();
							}
							iiNode.Type = type;
							Advance(); // over 'this'
							iiNode.Params = ParseParamList(TokenID.LBracket, TokenID.RBracket);

							//Boolean hasGetter = false;
							//Boolean hasSetter = false;
                            Boolean[] bx=new Boolean[2];

                            ParseInterfaceAccessors(bx);
							iiNode.HasGetter = bx[0];
							iiNode.HasSetter = bx[1];
							break;

						default:
							IdentifierExpression name = ParseQualifiedIdentifier();
							if (name == null)
							{
								ReportError("Expected Name or ident in member definition.");
							}
							switch (curtok.ID)
							{
								case TokenID.LParen:
									// method
									InterfaceMethodNode mnode = new InterfaceMethodNode();
									curInterface.Methods.add(mnode);

									if (curAttributes.size() > 0)
									{
										mnode.Attributes = curAttributes;
										curAttributes = new NodeCollection<AttributeNode>();
									}

									mnode.Modifiers = curmods;
									curmods = Modifier.Empty;

									mnode.Names.add(name);
									mnode.Type = type;
									mnode.Params = ParseParamList();

									AssertAndAdvance(TokenID.Semi);
									break;

								case TokenID.LCurly:
									// property
									InterfacePropertyNode pnode = new InterfacePropertyNode();
									curInterface.Properties.add(pnode);

									// these are the prop nodes
									if (curAttributes.size() > 0)
									{
										pnode.Attributes = curAttributes;
										curAttributes = new NodeCollection<AttributeNode>();
									}

									pnode.Modifiers = curmods;
									curmods = Modifier.Empty;

									pnode.Names.add(name);
									pnode.Type = type;

                                    bx=new Boolean[2];

                                    ParseInterfaceAccessors(bx);

									ParseInterfaceAccessors(bx);
									pnode.HasGetter = bx[0];
									pnode.HasSetter = bx[1];

									if (curtok.ID == TokenID.Semi)
									{
										AssertAndAdvance(TokenID.Semi);
									}
									break;

								default:
									ReportError("Invalid interface member syntax.");
									break;
							}
							break;
					}
					break;
			}
		}
		private void ParseCtor(TypeNode type)
		{
			ConstructorNode node = new ConstructorNode();

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			if ((curmods & Modifier.Static) != Modifier.Empty)
			{
				node.IsStaticConstructor = true;
				curmods = curmods & ~Modifier.Static;
			}
			long mask = ~(long)Modifier.ConstructorMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("constructor declaration contains illegal Modifiers");

			typeStack.peek().Constructors.add(node);
			//node.Attributes.add(curAttributes);
			//curAttributes.Clear();
			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			node.Type = type;
			node.Names.add(typeStack.peek().Name);

			// starts at LParen
			node.Params = ParseParamList();

			if (curtok.ID == TokenID.Colon)
			{
				Advance();
				if (curtok.ID == TokenID.Base)
				{
					Advance();
					node.HasBase = true;
					node.ThisBaseArgs = ParseArgs();
				}
				else if (curtok.ID == TokenID.This)
				{
					Advance();
					node.HasThis = true;
					node.ThisBaseArgs = ParseArgs();
				}
				else
				{
					RecoverFromError("constructor requires this or base calls after colon", TokenID.Base);
				}
			}
			ParseBlock(node.StatementBlock);
		}
		private void ParseDestructor()
		{
			Advance(); // over tilde

			DestructorNode node = new DestructorNode();

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}
			long mask = ~(long)Modifier.DestructorMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("destructor declaration contains illegal Modifiers");

			typeStack.peek().Destructors.add(node);

			node.Modifiers = curmods;
			curmods = Modifier.Empty;
			if (curtok.ID == TokenID.Ident)
			{
				node.Names.add( ParseQualifiedIdentifier() );
			}
			else
			{
				ReportError("Destructor requires identifier as Name.");
			}
			// no args in dtor
			AssertAndAdvance(TokenID.LParen);
			AssertAndAdvance(TokenID.RParen);

			ParseBlock(node.StatementBlock);
		}
		private void ParseOperatorDecl(TypeNode type)
		{
			OperatorNode node = new OperatorNode();

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			long mask = ~(long)Modifier.OperatorMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("operator declaration contains illegal Modifiers");

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			if (type == null && curtok.ID == TokenID.Explicit)
			{
				Advance();
				node.IsExplicit = true;
				AssertAndAdvance(TokenID.Operator);
				type = ParseType();
			}
			else if (type == null && curtok.ID == TokenID.Implicit)
			{
				Advance();
				node.IsImplicit = true;
				AssertAndAdvance(TokenID.Operator);
				type = ParseType();
			}
			else
			{
				AssertAndAdvance(TokenID.Operator);
				node.Operator = curtok.ID;
				Advance();
			}
			NodeCollection<ParamDeclNode> paramList = ParseParamList();
			if (paramList.size() == 0 || paramList.size() > 2)
			{
				ReportError("Operator declarations must only have one or two parameters.");
			}
			node.Param1 = paramList.get(0);
			if (paramList.size() == 2)
			{
				node.Param2 = paramList.get(1);
			}
			ParseBlock(node.Statements);
		}
		private void ParseIndexer(TypeNode type, IdentifierExpression interfaceType)
		{
			IndexerNode node = new IndexerNode();
			typeStack.peek().Indexers.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			long mask = ~(long)Modifier.IndexerMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("indexer declaration contains illegal Modifiers");


			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			node.Type = type;
			if (interfaceType != null)
			{
				node.InterfaceType = new TypeNode(interfaceType);
			}

			AssertAndAdvance(TokenID.This);
			node.Params = ParseParamList(TokenID.LBracket, TokenID.RBracket);

			// parse accessor part
			AssertAndAdvance(TokenID.LCurly);
			if (curtok.ID != TokenID.Ident)
			{
				RecoverFromError("At least one get or set required in accessor", curtok.ID);
			}
			boolean parsedGet = false;
			if (strings.get(curtok.Data).equals("get"))
			{
				node.Getter = ParseAccessor();
				parsedGet = true;
			}
			if (curtok.ID == TokenID.Ident && strings.get(curtok.Data).equals("set"))
			{
				node.Getter = ParseAccessor();
			}
			// get might follow set
			if (!parsedGet && curtok.ID == TokenID.Ident && strings.get(curtok.Data).equals("get"))
			{
				node.Getter = ParseAccessor();
			}
			AssertAndAdvance(TokenID.RCurly);
		}
		private void ParseMethod(TypeNode type, IdentifierExpression name)
		{
			long mask = ~(long)Modifier.MethodMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("method declaration contains illegal Modifiers");

			MethodNode node = new MethodNode();
			typeStack.peek().Methods.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			node.Type = type;
			node.Names.add(name);

			// starts at LParen
			node.Params = ParseParamList();

			ParseBlock(node.StatementBlock);

		}
		private void ParseField(TypeNode type, IdentifierExpression name)
		{
			long mask = ~(long)Modifier.FieldMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("field declaration contains illegal Modifiers");

			FieldNode node = new FieldNode();
			typeStack.peek().Fields.add(node);
			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Type = type;
			node.Names.add(name);

			//eg: int ok = 0, error, xx = 0;
			if (curtok.ID == TokenID.Equal)
			{
				Advance();
				node.Value = ParseConstExpr();
				if (curtok.ID == TokenID.Comma)
				{
					node = new FieldNode();
					typeStack.peek().Fields.add(node);
					node.Modifiers = curmods;
					node.Type = type;
				}
			}

			while (curtok.ID == TokenID.Comma)
			{
				Advance(); // over comma
				IdentifierExpression ident = ParseQualifiedIdentifier();
				node.Names.add(ident);
				if (curtok.ID == TokenID.Equal)
				{
					Advance();
					node.Value = ParseConstExpr();

					if (curtok.ID == TokenID.Comma)
					{
						node = new FieldNode();
						typeStack.peek().Fields.add(node);
						node.Modifiers = curmods;
						node.Type = type;
					}
				}
			}

			 if (curtok.ID == TokenID.Semi)
			{
				Advance();
			}



		}
		private void ParseProperty(TypeNode type, IdentifierExpression name)
		{
            long mask = ~(long)Modifier.PropertyMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("field declaration contains illegal Modifiers");

			PropertyNode node = new PropertyNode();
			typeStack.peek().Properties.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			node.Type = type;
			node.Names.add(name);

			// opens on lcurly
			AssertAndAdvance(TokenID.LCurly);

			// todo: AddNode Attributes to get and setters
			ParsePossibleAttributes(false);

			if (curAttributes.size() > 0)
			{
				//node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			if (curtok.ID != TokenID.Ident)
			{
				RecoverFromError("At least one get or set required in accessor", curtok.ID);
			}

			boolean parsedGet = false;
			if (strings.get(curtok.Data).equals("get"))
			{
				node.Getter = ParseAccessor();
				parsedGet = true;
			}

			// todo: AddNode Attributes to get and setters
			ParsePossibleAttributes(false);

			if (curAttributes.size() > 0)
			{
				//node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			if (curtok.ID == TokenID.Ident && strings.get(curtok.Data).equals("set"))
			{
				node.Setter = ParseAccessor();
			}

			// todo: AddNode Attributes to get and setters
			ParsePossibleAttributes(false);

			if (curAttributes.size() > 0)
			{
				//node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			// get might follow set
			if (!parsedGet && curtok.ID == TokenID.Ident && strings.get(curtok.Data) .equals("get"))
			{
				node.Getter = ParseAccessor();
			}

			AssertAndAdvance(TokenID.RCurly);
		}
		private void ParseEvent()
		{
			long mask = ~(long)Modifier.EventMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("Event contains illegal Modifiers");

			EventNode node = new EventNode();
			typeStack.peek().Events.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			Advance(); // advance over event keyword

			node.Type = ParseType();

			if (curtok.ID != TokenID.Ident)
				ReportError("Expected event member Name.");

			while (curtok.ID == TokenID.Ident)
			{
				node.Names.add(ParseQualifiedIdentifier());
			}
			if (curtok.ID == TokenID.LCurly)
			{
				Advance(); // over lcurly
				// todo: may be Attributes
				if (curtok.ID != TokenID.Ident)
				{
					ReportError("Event accessor requires add or remove clause.");
				}
				String curAccessor = strings.get(curtok.Data);
				Advance(); // over ident
				if (curAccessor.equals("add"))
				{
					ParseBlock(node.AddBlock);
					if (curtok.ID == TokenID.Ident && strings.get(curtok.Data).equals("remove"))
					{
						Advance(); // over ident
						ParseBlock(node.RemoveBlock);
					}
					else
					{
						ReportError("Event accessor expected remove clause.");
					}
				}
				else if (curAccessor.equals("remove"))
				{
					ParseBlock(node.RemoveBlock);
					if (curtok.ID == TokenID.Ident && strings.get(curtok.Data).equals("add"))
					{
						Advance(); // over ident
						ParseBlock(node.AddBlock);
					}
					else
					{
						ReportError("Event accessor expected add clause.");
					}
				}
				else
				{
					ReportError("Event accessor requires add or remove clause.");
				}
			}
			else
			{
				AssertAndAdvance(TokenID.Semi);
			}


		}
		private void ParseConst()
		{
			long mask = ~(long)Modifier.ConstantMods;
			if (((long)curmods & mask) != (long)Modifier.Empty)
				ReportError("const declaration contains illegal Modifiers");

			ConstantNode node = new ConstantNode();
			typeStack.peek().Constants.add(node);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Modifiers = curmods;
			curmods = Modifier.Empty;

			Advance(); // advance over const keyword

			node.Type = ParseType();

			boolean hasEqual = false;
			node.Names.add(ParseQualifiedIdentifier());
			if (curtok.ID == TokenID.Equal)
			{
				Advance();
				hasEqual = true;
			}
			while (curtok.ID == TokenID.Comma)
			{
				Advance();
				node.Names.add(ParseQualifiedIdentifier());
				if (curtok.ID == TokenID.Equal)
				{
					Advance();
					hasEqual = true;
				}
				else
				{
					hasEqual = false;
				}
			}

			if (hasEqual)
			{
				node.Value = ParseConstExpr();
			}

			AssertAndAdvance(TokenID.Semi);
		}
		private EnumNode ParseEnumMember()
		{
			EnumNode result = new EnumNode();

			ParsePossibleAttributes(false);

			if (curAttributes.size() > 0)
			{
				result.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			if (curtok.ID != TokenID.Ident)
			{
				ReportError("Enum members must be legal identifiers.");
			}
			String name = strings.get(curtok.Data);
			result.Name = new IdentifierExpression(new String[] { name });
			Advance();

			if (curtok.ID == TokenID.Equal)
			{
				Advance();
				result.Value = ParseExpression();
			}
			if (curtok.ID == TokenID.Comma)
			{
				Advance();
			}
			return result;

		}

		// member helpers
		private NodeCollection<ParamDeclNode> ParseParamList()
		{
			// default is parens, however things like indexers use square brackets
			return ParseParamList(TokenID.LParen, TokenID.RParen);
		}
		private NodeCollection<ParamDeclNode> ParseParamList(int openToken, int closeToken)
		{
			AssertAndAdvance(openToken);
			if (curtok.ID == closeToken)
			{
				Advance();
				return null;
			}
			NodeCollection<ParamDeclNode> result = new NodeCollection<ParamDeclNode>();
			boolean isParams = false;
			boolean hasComma = false;
			do
			{
				ParamDeclNode node = new ParamDeclNode();
				result.add(node);
				isParams = false;

				ParsePossibleAttributes(false);

				if (curtok.ID == TokenID.Ref)
				{
					node.Modifiers |= Modifier.Ref;
					Advance();
				}
				else if (curtok.ID == TokenID.Out)
				{
					node.Modifiers |= Modifier.Out;
					Advance();
				}
				else if (curtok.ID == TokenID.Params)
				{
					isParams = true;
					node.Modifiers |= Modifier.Params;
					Advance();
				}

				node.Type = ParseType();

				if (isParams)
				{
					// ensure is array type
				}

				if (curtok.ID == TokenID.Ident)
				{
					node.Name = strings.get(curtok.Data);
					Advance();
				}

				hasComma = false;
				if (curtok.ID == TokenID.Comma)
				{
					Advance();
					hasComma = true;
				}
			}
			while (!isParams && hasComma);

			AssertAndAdvance(closeToken);

			return result;
		}
		private ParamDeclNode ParseParamDecl()
		{

			ParamDeclNode node = new ParamDeclNode();

			ParsePossibleAttributes(false);

			if (curAttributes.size() > 0)
			{
				node.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			node.Type = ParseType();

			if (curtok.ID == TokenID.Ident)
			{
				node.Name = strings.get(curtok.Data);
				Advance();
			}
			else
			{
				RecoverFromError("Expected arg Name.", TokenID.Ident);
			}
			return node;
		}
		private NodeCollection<ArgumentNode> ParseArgs()
		{
			AssertAndAdvance(TokenID.LParen);
			if (curtok.ID == TokenID.RParen)
			{
				Advance();
				return null;
			}
			boolean hasComma = false;
			NodeCollection<ArgumentNode> result = new NodeCollection<ArgumentNode>();
			do
			{
				ArgumentNode node = new ArgumentNode();
				result.add(node);

				if (curtok.ID == TokenID.Ref)
				{
					node.IsRef = true;
					Advance();
				}
				else if (curtok.ID == TokenID.Out)
				{
					node.IsOut = true;
					Advance();
				}
				node.Expression = ParseExpression();

				hasComma = false;
				if (curtok.ID == TokenID.Comma)
				{
					Advance();
					hasComma = true;
				}
			}
			while (hasComma);

			AssertAndAdvance(TokenID.RParen);

			return result;
		}
		private AccessorNode ParseAccessor()
		{
			AccessorNode result = new AccessorNode();

			ParsePossibleAttributes(false);

			if (curAttributes.size() > 0)
			{
				result.Attributes = curAttributes;
				curAttributes = new NodeCollection<AttributeNode>();
			}

			String kind = "";
			if (curtok.ID == TokenID.Ident)
			{
				kind = strings.get(curtok.Data);
			}
			else
			{
				RecoverFromError("Must specify accessor kind in accessor.", curtok.ID);
			}

			result.Kind = kind;
			Advance();
			if (curtok.ID == TokenID.Semi)
			{
				result.IsAbstractOrInterface = true;
				Advance(); // over semi
			}
			else
			{
				ParseBlock(result.StatementBlock);
			}
			return result;
		}
		private ConstantExpression ParseConstExpr()
		{
			ConstantExpression node = new ConstantExpression();
			node.Value = ParseExpression();

			return node;
		}
		private void ParseModifiers()
		{
			while(!curtok.equals(EOF))
			{
				switch (curtok.ID)
				{
					case TokenID.New:
					case TokenID.Public:
					case TokenID.Protected:
					case TokenID.Internal:
					case TokenID.Private:
					case TokenID.Abstract:
					case TokenID.Sealed:
					case TokenID.Static:
					case TokenID.Virtual:
					case TokenID.Override:
					case TokenID.Extern:
					case TokenID.Readonly:
					case TokenID.Volatile:
					case TokenID.Ref:
					case TokenID.Out:
                    //Newly added
                    case TokenID.Partial:
					//case TokenID.Assembly:
					//case TokenID.Field:
					//case TokenID.Event:
					//case TokenID.Method:
					//case TokenID.Param:
					//case TokenID.Property:
					//case TokenID.Return:
					//case TokenID.Type:

						long mod = (long)modMap.get(curtok.ID);
						if (((long)curmods & mod) > 0)
						{
							ReportError("Duplicate modifier.");
						}
						curmods |= mod;
						Advance();
						break;


					default:
						return;
				}
			}
		}
		private long ParseAttributeModifiers()
		{
			long result = Modifier.Empty;
			String curIdent = "";
			boolean isMod = true;
			while (isMod)
			{
				switch (curtok.ID)
				{
					case TokenID.Ident:
						curIdent = strings.get(curtok.Data);

                        if(curIdent.equals("field")){
                            result |= Modifier.Field;
                        }else if(curIdent.equals("method")){
                            result |= Modifier.Method;
                        }else if(curIdent.equals("param")){
                            result |= Modifier.Param;
                        }else if(curIdent.equals("property")){
                            result |= Modifier.Property;
                        }else if(curIdent.equals("type")){
                            result |= Modifier.Type;
                        }else if(curIdent.equals("module")){
                            result |= Modifier.Module;
                        }else if(curIdent.equals("assembly")){
                            result |= Modifier.Assembly;
                        }else{
                            isMod = false;
                        }
                        Advance();


//                        switch (curIdent)
//						{
//							case "field":
//								result |= Modifier.Field;
//								Advance();
//								break;
//							case "method":
//								result |= Modifier.Method;
//								Advance();
//								break;
//							case "param":
//								result |= Modifier.Param;
//								Advance();
//								break;
//							case "property":
//								result |= Modifier.Property;
//								Advance();
//								break;
//							case "type":
//								result |= Modifier.Type;
//								Advance();
//								break;
//							case "module":
//								result |= Modifier.Module;
//								Advance();
//								break;
//							case "assembly":
//								result |= Modifier.Assembly;
//								Advance();
//								break;
//							default:
//								isMod = false;
//								break;
//						}
						break;

					case TokenID.Return:
						result |= Modifier.Return;
						Advance();
						break;

					case TokenID.Event:
						result |= Modifier.Event;
						Advance();
						break;

					default:
						isMod = false;
						break;

				}
			}
			return result;
		}
		private TypeNode ParseType()
		{
			IdentifierExpression idPart = ParseQualifiedIdentifier();
			TypeNode result = new TypeNode(idPart);

			// now any 'rank only' specifiers (without size decls)
			while (curtok.ID == TokenID.LBracket)
			{
				if (index < tokens.size() &&
					tokens.get(index).ID != TokenID.RBracket &&
					tokens.get(index).ID != TokenID.Comma)
				{
					// anything with size or accessor decls has own node type
					break;
				}
				Advance(); // over lbracket
				int commaCount = 0;
				while (curtok.ID == TokenID.Comma)
				{
					commaCount++;
					Advance();
				}
				result.RankSpecifiers.add(commaCount);
				AssertAndAdvance(TokenID.RBracket);
			}

			return result;
		}
		private IdentifierExpression ParseQualifiedIdentifier()
		{
			IdentifierExpression result = new IdentifierExpression();
			List<String> qualName = new ArrayList<String>();
			switch (curtok.ID)
			{
				case TokenID.Ident:
					qualName.add(strings.get(curtok.Data));
					Advance();
					break;

				case TokenID.Bool:
				case TokenID.Byte:
				case TokenID.Char:
				case TokenID.Decimal:
				case TokenID.Double:
				case TokenID.Float:
				case TokenID.Int:
				case TokenID.Long:
				case TokenID.Object:
				case TokenID.SByte:
				case TokenID.Short:
				case TokenID.String:
				case TokenID.UInt:
				case TokenID.ULong:
				case TokenID.UShort:
				case TokenID.Void:
				case TokenID.This:
				case TokenID.Base:

                    //String predef = Enum.getName(TokenID.Invalid.GetType(), curtok.ID).ToLower();
					qualName.add(TokenID.getFieldName(curtok.ID));
					result.StartingPredefinedType = curtok.ID;
					Advance();
					break;

				default:
                    RecoverFromError(TokenID.Ident);
					break;
			}

			while (curtok.ID == TokenID.Dot)
			{
				Advance();
				if (curtok.ID == TokenID.Ident)
				{
					qualName.add(strings.get(curtok.Data));
					Advance();
				}
				else if (curtok.ID == TokenID.This)
				{
					// this is an indexer with a prepended interface, do nothing (but consume dot)
				}
				else
				{
                    RecoverFromError(TokenID.Ident);
				}
			}

//			result.Identifier = (String[])qualName.toArray(String[] result.Identifier);
            result.Identifier=new String[qualName.toArray().length];
            qualName.toArray(result.Identifier);
			return result;
		}
		private IdentifierExpression ParseIdentifierOrKeyword()
		{
			IdentifierExpression result = new IdentifierExpression();
			switch (curtok.ID)
			{
				case TokenID.Ident:
					result.Identifier = new String[] { strings.get(curtok.Data) };
					Advance();
					break;

				case TokenID.If:
				case TokenID.Else:
				case TokenID.Bool:
				case TokenID.Byte:
				case TokenID.Char:
				case TokenID.Decimal:
				case TokenID.Double:
				case TokenID.Float:
				case TokenID.Int:
				case TokenID.Long:
				case TokenID.Object:
				case TokenID.SByte:
				case TokenID.Short:
				case TokenID.String:
				case TokenID.UInt:
				case TokenID.ULong:
				case TokenID.UShort:
				case TokenID.Void:
				case TokenID.This:
				case TokenID.Base:
					String predef = TokenID.getFieldName(curtok.ID);//Enum.GetName(TokenID.Invalid.GetType(), curtok.ID).ToLower();
					result.Identifier = new String[] { predef };
					result.StartingPredefinedType = curtok.ID;
					Advance();
					break;

				default:
                    RecoverFromError(TokenID.Ident);
					break;
			}
			return result;
		}

    private void ParseInterfaceAccessors(Boolean[] bx)
		{
			AssertAndAdvance(TokenID.LCurly); // LCurly

			// the get and set can also have Attributes
			ParsePossibleAttributes(false);

			if (curtok.ID == TokenID.Ident && strings.get(curtok.Data).equals( "get"))
			{
				if (curAttributes.size() > 0)
				{
					// todo: store get/set Attributes on InterfacePropertyNode
					// pnode.getAttributes = curAttributes;
					curAttributes = new NodeCollection<AttributeNode>();
				}

				bx[0] = true;
				Advance();
				AssertAndAdvance(TokenID.Semi);
				if (curtok.ID == TokenID.Ident)
				{
					if (strings.get(curtok.Data).equals( "set"))
					{
						bx[1] = true;
						Advance();
						AssertAndAdvance(TokenID.Semi);
					}
					else
					{
						RecoverFromError("Expected set in interface property def.", curtok.ID);
					}
				}
			}
			else if (curtok.ID == TokenID.Ident && strings.get(curtok.Data).equals( "set"))
			{
				if (curAttributes.size() > 0)
				{
					// todo: store get/set Attributes on InterfacePropertyNode
					// pnode.setAttributes = curAttributes;
					curAttributes = new NodeCollection<AttributeNode>();
				}
				bx[1] = true;
				Advance();
				AssertAndAdvance(TokenID.Semi);
				if (curtok.ID == TokenID.Ident)
				{
					if (strings.get(curtok.Data).equals("get"))
					{
						bx[0] = true;
						Advance();
						AssertAndAdvance(TokenID.Semi);
					}
					else
					{
						RecoverFromError("Expected get in interface property def.", curtok.ID);
					}
				}
			}
			else
			{
				RecoverFromError("Expected get or set in interface property def.", curtok.ID);
			}

			AssertAndAdvance(TokenID.RCurly);
		}

		// statements
		private void ParseStatement(NodeCollection<StatementNode> node)
		{
			// label		ident	: colon
			// localDecl	type	: ident
			// block		LCurly
			// empty		Semi
			// expression
			//	-invoke		pexpr	: LParen
			//	-objCre		new		: type
			//	-assign		uexpr	: assignOp
			//	-postInc	pexpr	: ++
			//	-postDec	pexpr	: --
			//	-preInc		++		: uexpr
			//	-preDec		--		: uexpr
			//
			// selection	if		: LParen
			//				switch	: LParen
			//
			// iteration	while	: LParen
			//				do		: LParen
			//				for		: LParen
			//				foreach	: LParen
			//
			// jump			break	: Semi
			//				continue: Semi
			//				goto	: ident | case | default
			//				return	: expr
			//				throw	: expr
			//
			// try			try		: block
			// checked		checked	: block
			// unchecked	unchecked : block
			// lock			lock	: LParen
			// using		using	: LParen
			switch (curtok.ID)
			{
				case TokenID.LCurly:	// block
					BlockStatement newBlock = new BlockStatement();
					node.add(newBlock);
					ParseBlock(newBlock);
					break;
				case TokenID.Semi:		// empty statement
					Advance();
					node.add(new StatementNode());
					break;
				case TokenID.If:		// If statement
					node.add(ParseIf());
					break;
				case TokenID.Switch:	// Switch statement
					node.add(ParseSwitch());
					break;
				case TokenID.While:		// While statement
					node.add(ParseWhile());
					break;
				case TokenID.Do:		// Do statement
					node.add(ParseDo());
					break;
				case TokenID.For:		// For statement
					node.add(ParseFor());
					break;
				case TokenID.Foreach:	// Foreach statement
					node.add(ParseForEach());
					break;
				case TokenID.Break:		// Break statement
					node.add(ParseBreak());
					break;
				case TokenID.Continue:	// Continue statement
					node.add(ParseContinue());
					break;
				case TokenID.Goto:		// Goto statement
					node.add(ParseGoto());
					break;
				case TokenID.Return:	// Return statement
					node.add(ParseReturn());
					break;
				case TokenID.Throw:		// Throw statement
					node.add(ParseThrow());
					break;
				case TokenID.Try:		// Try statement
					node.add(ParseTry());
					break;
				case TokenID.Checked:	// Checked statement
					node.add(ParseChecked());
					break;
				case TokenID.Unchecked:	// Unchecked statement
					node.add(ParseUnchecked());
					break;
				case TokenID.Lock:		// Lock statement
					node.add(ParseLock());
					break;
				case TokenID.Using:		// Using statement
					node.add(ParseUsing());
					break;

				case TokenID.Const:
					isLocalConst = true;
					Advance();
					break;

				case TokenID.Bool:
				case TokenID.Byte:
				case TokenID.Char:
				case TokenID.Decimal:
				case TokenID.Double:
				case TokenID.Float:
				case TokenID.Int:
				case TokenID.Long:
				case TokenID.Object:
				case TokenID.SByte:
				case TokenID.Short:
				case TokenID.String:
				case TokenID.UInt:
				case TokenID.ULong:
				case TokenID.UShort:

				case TokenID.StringLiteral:
				case TokenID.HexLiteral:
				case TokenID.IntLiteral:
				case TokenID.UIntLiteral:
				case TokenID.LongLiteral:
				case TokenID.ULongLiteral:
				case TokenID.TrueLiteral:
				case TokenID.FalseLiteral:
				case TokenID.NullLiteral:
				case TokenID.LParen:
				case TokenID.DecimalLiteral:
				case TokenID.RealLiteral:
				case TokenID.CharLiteral:
				case TokenID.PlusPlus:	// PreInc statement
				case TokenID.MinusMinus:// PreDec statement
				case TokenID.This:
				case TokenID.Base:
				case TokenID.New:		// creation statement
					ExpressionStatement enode = new ExpressionStatement(ParseExpression());
					node.add(enode);
					if (curtok.ID == TokenID.Semi)
					{
						Advance();
					}
					break;

				case TokenID.Ident:
					if (tokens.size() > index + 1 && tokens.get(index).ID == TokenID.Colon)
					{
						LabeledStatement lsnode = new LabeledStatement();
						lsnode.Name = ParseQualifiedIdentifier();
						AssertAndAdvance(TokenID.Colon);
						ParseStatement(lsnode.Statements);
						node.add(lsnode);
					}
					else
					{
						ExpressionStatement inode = new ExpressionStatement(ParseExpression());
						node.add(inode);
					}
					if (curtok.ID == TokenID.Semi)
					{
						Advance();
					}
					break;

				case TokenID.Unsafe:
					// preprocessor directives
					ParseUnsafeCode();
					break;

				default:
                    System.out.println("Unhandled case in statement parsing: \"" + curtok.ID + "\" in line: " + lineCount);
					// this is almost always an expression
					ExpressionStatement dnode = new ExpressionStatement(ParseExpression());
					node.add(dnode);
					if (curtok.ID == TokenID.Semi)
					{
						Advance();
					}
					break;
			}
		}
		private void ParseBlock(BlockStatement node)
		{
			ParseBlock(node, false);
		}
		private void ParseBlock(BlockStatement node, boolean isCase)
		{
			if (curtok.ID == TokenID.LCurly)
			{
				Advance(); // over lcurly
				while (curtok.ID != TokenID.Eof && curtok.ID != TokenID.RCurly)
				{
					ParseStatement(node.Statements);
				}
				AssertAndAdvance(TokenID.RCurly);
			}
			else if(isCase)
			{
				// case stmts can have multiple lines without curlies, ugh
				// break can be omitted if it is unreachable code, double ugh
				// this becomes impossible to trace without code analysis of course, so look for 'case' or '}'

				while (curtok.ID != TokenID.Eof && curtok.ID != TokenID.Case && curtok.ID != TokenID.Default && curtok.ID != TokenID.RCurly)
				{
					ParseStatement(node.Statements);
				}
				//boolean endsOnReturn = false;
				//while (curtok.ID != TokenID.Eof && !endsOnReturn)
				//{
				//    TokenID startTok = curtok.ID;
				//    if (startTok == TokenID.Return	||
				//        startTok == TokenID.Goto	||
				//        startTok == TokenID.Throw	||
				//        startTok == TokenID.Break)
				//    {
				//        endsOnReturn = true;
				//    }

				//    ParseStatement(node.Statements);

				//    // doesn't have to end on return or goto
				//    if (endsOnReturn && (startTok == TokenID.Return	|| startTok == TokenID.Goto	|| startTok == TokenID.Throw))
				//    {
				//        if (curtok.ID == TokenID.Break)
				//        {
				//            ParseStatement(node.Statements);
				//        }
				//    }
				//}
			}
			else
			{
				ParseStatement(node.Statements);
			}

		}
		private IfStatement ParseIf()
		{
			IfStatement node = new IfStatement();
			Advance(); // advance over IF

			AssertAndAdvance(TokenID.LParen);
			node.Test = ParseExpression();
			AssertAndAdvance(TokenID.RParen);

			ParseBlock(node.Statements);

			if (curtok.ID == TokenID.Else)
			{
				Advance(); // advance of else
				ParseBlock(node.ElseStatements);
			}
			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private SwitchStatement ParseSwitch()
		{
			SwitchStatement node = new SwitchStatement();
			Advance(); // advance over SWITCH

			AssertAndAdvance(TokenID.LParen);
			node.Test = ParseExpression();
			AssertAndAdvance(TokenID.RParen);

			AssertAndAdvance(TokenID.LCurly);
			while (curtok.ID == TokenID.Case || curtok.ID == TokenID.Default)
			{
				node.Cases.add(ParseCase());
			}

			AssertAndAdvance(TokenID.RCurly);

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private CaseNode ParseCase()
		{
			CaseNode node = new CaseNode();
			boolean isDefault = (curtok.ID == TokenID.Default);
			Advance(); // advance over CASE or DEFAULT

			if (!isDefault)
			{
				node.Ranges.add(ParseExpression());
			}
			else
			{
				node.IsDefaultCase = true;
			}
			AssertAndAdvance(TokenID.Colon);

			// may be multiple cases, but must be at least one
			while (curtok.ID == TokenID.Case || curtok.ID == TokenID.Default)
			{
				isDefault = (curtok.ID == TokenID.Default);
				Advance(); // advance over CASE or DEFAULT
				if (!isDefault)
				{
					node.Ranges.add(ParseExpression());
				}
				else
				{
					node.IsDefaultCase = true;
				}
				AssertAndAdvance(TokenID.Colon);
			}
			if (curtok.ID != TokenID.LCurly)
			{
				node.StatementBlock.setHasBraces(false);
			}
			ParseBlock(node.StatementBlock, true);
			return node;
		}
		private WhileStatement ParseWhile()
		{
			WhileStatement node = new WhileStatement();
			Advance(); // advance over While

			AssertAndAdvance(TokenID.LParen);
			node.Test = ParseExpression();
			AssertAndAdvance(TokenID.RParen);

			ParseBlock(node.Statements);

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private DoStatement ParseDo()
		{
			DoStatement node = new DoStatement();
			Advance(); // advance over DO

			ParseBlock(node.Statements);

			AssertAndAdvance(TokenID.While); // advance over While

			AssertAndAdvance(TokenID.LParen);
			node.Test = ParseExpression();
			AssertAndAdvance(TokenID.RParen);

			AssertAndAdvance(TokenID.Semi); // not optional on DO

			return node;
		}
		private ForStatement ParseFor()
		{
			ForStatement node = new ForStatement();
			Advance(); // advance over FOR

			AssertAndAdvance(TokenID.LParen);

			if (curtok.ID != TokenID.Semi)
			{
				node.Init.add(ParseExpression());
				while (curtok.ID == TokenID.Comma)
				{
					AssertAndAdvance(TokenID.Comma);
					node.Init.add(ParseExpression());
				}
			}
			AssertAndAdvance(TokenID.Semi);

			if (curtok.ID != TokenID.Semi)
			{
				node.Test.add(ParseExpression());
				while (curtok.ID == TokenID.Comma)
				{
					AssertAndAdvance(TokenID.Comma);
					node.Test.add(ParseExpression());
				}
			}
			AssertAndAdvance(TokenID.Semi);

			if (curtok.ID != TokenID.RParen)
			{
				node.Inc.add(ParseExpression());
				while (curtok.ID == TokenID.Comma)
				{
					AssertAndAdvance(TokenID.Comma);
					node.Inc.add(ParseExpression());
				}
			}
			AssertAndAdvance(TokenID.RParen);
			ParseBlock(node.Statements);

			if (curtok.ID == TokenID.Semi)
			{
				Advance();
			}
			return node;
		}
		private ForEachStatement ParseForEach()
		{
			ForEachStatement node = new ForEachStatement();
			Advance(); // advance over FOREACH

			AssertAndAdvance(TokenID.LParen);
			node.Iterator = ParseParamDecl();
			AssertAndAdvance(TokenID.In);
			node.Collection = ParseExpression();
			AssertAndAdvance(TokenID.RParen);

			//node.Statements = ParseBlock().Statements;

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private BreakStatement ParseBreak()
		{
			BreakStatement node = new BreakStatement();
			Advance(); // advance over BREAK

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private ContinueStatement ParseContinue()
		{
			ContinueStatement node = new ContinueStatement();
			Advance(); // advance over Continue

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private GotoStatement ParseGoto()
		{
			Advance();
			GotoStatement gn = new GotoStatement();
			if (curtok.ID == TokenID.Case)
			{
				Advance();
				gn.IsCase = true;
			}
			else if (curtok.ID == TokenID.Default)
			{
				Advance();
				gn.IsDefaultCase = true;
			}
			if (!gn.IsDefaultCase)
			{
				gn.Target = ParseExpression();
			}
			AssertAndAdvance(TokenID.Semi);
			return gn;
		}
		private ReturnStatement ParseReturn()
		{
			ReturnStatement node = new ReturnStatement();
			Advance(); // advance over Return

			if (curtok.ID == TokenID.Semi)
			{
				Advance();
			}
			else
			{
				node.ReturnValue = ParseExpression();
				AssertAndAdvance(TokenID.Semi);
			}
			return node;
		}
		private ThrowNode ParseThrow()
		{
			ThrowNode node = new ThrowNode();
			Advance(); // advance over Throw

			if (curtok.ID != TokenID.Semi)
			{
				node.ThrowExpression = ParseExpression();
			}

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private TryStatement ParseTry()
		{
			TryStatement node = new TryStatement();
			Advance(); // advance over Try
			ParseBlock(node.TryBlock);
			while (curtok.ID == TokenID.Catch)
			{
				CatchNode cn = new CatchNode();
				node.CatchBlocks.add(cn);

				Advance(); // over catch
				if (curtok.ID == TokenID.LParen)
				{
					Advance(); // over lparen
					cn.ClassType = ParseType();

					if (curtok.ID == TokenID.Ident)
					{
						cn.Identifier = new IdentifierExpression(new String[]{strings.get(curtok.Data)});
						Advance();
					}
					AssertAndAdvance(TokenID.RParen);
					ParseBlock(cn.CatchBlock);
				}
				else
				{
					ParseBlock(cn.CatchBlock);
					break; // must be last catch block if not a specific catch clause
				}
			}
			if (curtok.ID == TokenID.Finally)
			{
				Advance(); // over finally
				FinallyNode fn = new FinallyNode();
				node.FinallyBlock = fn;
				ParseBlock(fn.FinallyBlock);
			}

			if (curtok.ID == TokenID.Semi)
			{
				Advance();
			}
			return node;
		}
		private CheckedStatement ParseChecked()
		{
			CheckedStatement node = new CheckedStatement();
			Advance(); // advance over Checked

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private UncheckedStatement ParseUnchecked()
		{
			UncheckedStatement node = new UncheckedStatement();
			Advance(); // advance over Uncecked

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private LockStatement ParseLock()
		{
			LockStatement node = new LockStatement();
			Advance(); // advance over Lock

			AssertAndAdvance(TokenID.LParen);
			node.Target = ParseExpression();
			AssertAndAdvance(TokenID.RParen);
			ParseBlock(node.Statements);

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private UsingStatement ParseUsing()
		{
			UsingStatement node = new UsingStatement();
			Advance(); // advance over Using

			AssertAndAdvance(TokenID.LParen);
			node.Resource = ParseExpression();
			AssertAndAdvance(TokenID.RParen);
			ParseBlock(node.Statements);

			if (curtok.ID == TokenID.Semi)
				Advance();
			return node;
		}
		private void ParseUnsafeCode()
		{
			// todo: fully parse unsafe code

			Advance(); // over 'unsafe'
			AssertAndAdvance(TokenID.LCurly);

			int lcount = 1;
			while (curtok.ID != TokenID.Eof && lcount != 0)
			{
				Advance();
				if (curtok.ID == TokenID.RCurly)
				{
					lcount--;
				}
				else if (curtok.ID == TokenID.LCurly)
				{
					lcount++;
				}
			}
			if (curtok.ID != TokenID.Eof)
			{
				Advance(); // over RCurly
			}
		}

		// expressions
		private ExpressionNode ParseExpression(int endToken)
		{
			int id = curtok.ID;
			while (	id != endToken		&& id != TokenID.Eof	&&
					id != TokenID.Semi	&& id != TokenID.RParen &&
					id != TokenID.Comma && id != TokenID.Colon)
			{
				ParseExpressionSegment();
				id = curtok.ID;
			}
			return exprStack.pop();
		}
		private ExpressionNode ParseExpression()
		{
			int id = curtok.ID;
			while ( id != TokenID.Eof && id != TokenID.RCurly &&
					id != TokenID.Semi	&& id != TokenID.RParen &&
					id != TokenID.Comma && id != TokenID.Colon)
			{
				ParseExpressionSegment();
				id = curtok.ID;
			}
			return exprStack.pop();
		}
		private void ParseExpressionSegment()
		{
			// arraycre		new			: type : [{
			// literal		(lit)
			// simpleName	ident
			// parenExpr	LParen		: expr
			// memAccess	pexpr		: Dot
			//				pdefType	: Dot
			// invoke		pexpr		: LParen
			// elemAccess	noArrCreExpr: LBracket
			// thisAccess	this
			// baseAccess	base		: Dot
			//				base		: LBracket
			// postInc		pexpr		: ++
			// postDec		pexpr		: --
			// objCre		new			: type : LParen
			// delgCre		new			: delgType : LParen
			// typeof		typeof		: LParen
			// checked		checked		: LParen
			// unchecked	unchecked	: LParen
			ExpressionNode tempNode = null;
			int startToken = curtok.ID;
			switch (curtok.ID)
			{
				case TokenID.NullLiteral:
					exprStack.push(new NullPrimitive());
					Advance();
					break;

				case TokenID.TrueLiteral:
					exprStack.push(new BooleanPrimitive(true));
					Advance();
					ParseContinuingPrimary();
					break;

				case TokenID.FalseLiteral:
					exprStack.push(new BooleanPrimitive(false));
					Advance();
					ParseContinuingPrimary();
					break;

				case TokenID.IntLiteral:
					exprStack.push(new IntegralPrimitive(strings.get(curtok.Data), IntegralType.Int));
					Advance();
					ParseContinuingPrimary();
					break;
				case TokenID.UIntLiteral:
					exprStack.push(new IntegralPrimitive(strings.get(curtok.Data), IntegralType.UInt));
					Advance();
					ParseContinuingPrimary();
					break;
				case TokenID.LongLiteral:
					exprStack.push(new IntegralPrimitive(strings.get(curtok.Data), IntegralType.Long));
					Advance();
					ParseContinuingPrimary();
					break;
				case TokenID.ULongLiteral:
					exprStack.push(new IntegralPrimitive(strings.get(curtok.Data), IntegralType.ULong));
					Advance();
					ParseContinuingPrimary();
					break;

				case TokenID.RealLiteral:
					exprStack.push(new RealPrimitive(strings.get(curtok.Data)));
					Advance();
					ParseContinuingPrimary();
					break;

				case TokenID.CharLiteral:
					exprStack.push(new CharPrimitive(strings.get(curtok.Data)));
					Advance();
					ParseContinuingPrimary();
					break;

				case TokenID.StringLiteral:
					String sval = strings.get(curtok.Data);
					exprStack.push(new StringPrimitive(sval));
					Advance();
					ParseContinuingPrimary();
					break;

				case TokenID.Bool:
				case TokenID.Byte:
				case TokenID.Char:
				case TokenID.Decimal:
				case TokenID.Double:
				case TokenID.Float:
				case TokenID.Int:
				case TokenID.Long:
				case TokenID.Object:
				case TokenID.SByte:
				case TokenID.Short:
				case TokenID.String:
				case TokenID.UInt:
				case TokenID.ULong:
				case TokenID.UShort:
					IdentifierExpression qe = ParseQualifiedIdentifier();
					exprStack.push(qe);
					ParseContinuingPrimary();
					break;

				case TokenID.Plus:
					tempNode = ConsumeBinary(startToken);
					if (tempNode != null)
					{
						exprStack.push(new UnaryExpression(startToken, tempNode)); // unary
					}
					break;
				case TokenID.Minus:
					tempNode = ConsumeBinary(startToken);
					if (tempNode != null)
					{
						exprStack.push(new UnaryExpression(startToken, tempNode)); // unary
					}
					break;

				case TokenID.Is:
				case TokenID.As:
				case TokenID.Star:
				case TokenID.Slash:
				case TokenID.Percent:
				case TokenID.ShiftLeft:
				case TokenID.ShiftRight:
				case TokenID.Less:
				case TokenID.Greater:
				case TokenID.LessEqual:
				case TokenID.GreaterEqual:
				case TokenID.EqualEqual:
				case TokenID.NotEqual:
				case TokenID.BAnd:
				case TokenID.BXor:
				case TokenID.BOr:
				case TokenID.And:
				case TokenID.Or:
					ConsumeBinary(startToken);
					break;


				case TokenID.Not:
				case TokenID.Tilde:
				case TokenID.PlusPlus:
				case TokenID.MinusMinus:
					ConsumeUnary(startToken);
					break;

				case TokenID.Question:
					ExpressionNode condTest = exprStack.pop();
					Advance();
					ExpressionNode cond1 = ParseExpression(TokenID.Colon);
					AssertAndAdvance(TokenID.Colon);
					ExpressionNode cond2 = ParseExpression();

					exprStack.push(new ConditionalExpression(condTest, cond1, cond2));
					break;
				// keywords
				case TokenID.Ref:
					Advance();
					ParseExpressionSegment();
					exprStack.push(new RefNode(exprStack.pop()));
					break;

				case TokenID.Out:
					Advance();
					ParseExpressionSegment();
					exprStack.push(new OutNode(exprStack.pop()));
					break;

				case TokenID.This:
					exprStack.push(ParseQualifiedIdentifier());
					ParseContinuingPrimary();
					break;

				case TokenID.Void:
					// this can happen in typeof(void), nothing can follow
					Advance();
					exprStack.push(new VoidPrimitive());
					break;

				case TokenID.Base:
					Advance();
					int newToken = curtok.ID;
					if (newToken == TokenID.Dot)
					{
						Advance();
						String baseIdent = strings.get(curtok.Data);
						IdentifierExpression ide = new IdentifierExpression(new String[] { baseIdent });
						Advance();
						exprStack.push(new BaseAccessExpression(ide));

					}
					else if (newToken == TokenID.LBracket)
					{
						Advance();
						ExpressionList el = ParseExpressionList(TokenID.RBracket);
						exprStack.push(new BaseAccessExpression(el));
					}
					ParseContinuingPrimary();
					break;

				case TokenID.Typeof:
					Advance();
					AssertAndAdvance(TokenID.LParen);
					exprStack.push(new TypeOfExpression( ParseExpression(TokenID.RParen) ));
					AssertAndAdvance(TokenID.RParen);
					ParseContinuingPrimary();
					break;

				case TokenID.Checked:
					Advance();
					AssertAndAdvance(TokenID.LParen);
					ParseExpressionSegment();
					exprStack.push(new CheckedExpression(exprStack.pop()));
					AssertAndAdvance(TokenID.RParen);
					ParseContinuingPrimary();
					break;

				case TokenID.Unchecked:
					Advance();
					AssertAndAdvance(TokenID.LParen);
					ParseExpressionSegment();
					exprStack.push(new UncheckedExpression(exprStack.pop()));
					AssertAndAdvance(TokenID.RParen);
					ParseContinuingPrimary();
					break;

				case TokenID.Equal:
				case TokenID.PlusEqual:
				case TokenID.MinusEqual:
				case TokenID.StarEqual:
				case TokenID.SlashEqual:
				case TokenID.PercentEqual:
				case TokenID.BAndEqual:
				case TokenID.BOrEqual:
				case TokenID.BXorEqual:
				case TokenID.ShiftLeftEqual:
				case TokenID.ShiftRightEqual:
					int op = curtok.ID;
					Advance();


                    if (exprStack.size() > 0 && !(exprStack.peek() instanceof PrimaryExpression) && !(exprStack.peek() instanceof UnaryCastExpression))
					{
						ReportError("Left hand side of assignment must be a variable.");
					}
					ExpressionNode assignVar = exprStack.pop();
					ExpressionNode rightSide = ParseExpression();
					exprStack.push(new AssignmentExpression(op, assignVar, rightSide));
					break;


				case TokenID.LCurly:
					Advance();
					ArrayInitializerExpression aie = new ArrayInitializerExpression();
					exprStack.push(aie);
					aie.Expressions = ParseExpressionList(TokenID.RCurly);
					break;

				case TokenID.New:
					Advance();

					TypeNode newType = ParseType();
					if (curtok.ID == TokenID.LParen)
					{
						Advance();
						ExpressionList newList = ParseExpressionList(TokenID.RParen);
						exprStack.push(new ObjectCreationExpression(newType, newList));
					}
					else if (curtok.ID == TokenID.LBracket)
					{
						ParseArrayCreation(newType);
					}
					ParseContinuingPrimary();
					break;

				case TokenID.Ident:

					//test for local decl
					boolean isDecl = isAfterType();
					if (isDecl)
					{
						ParseLocalDeclaration();
					}
					else
					{
						exprStack.push(ParseQualifiedIdentifier());
						ParseContinuingPrimary();
					}
					break;

				case TokenID.LParen:
					Advance();
					ParseCastOrGroup();
					break;

				default:
					RecoverFromError("Unhandled case in ParseExpressionSegment", curtok.ID); // todo: fill out error report
					break;
			}
		}

		private void ConsumeUnary(int startOp)
		{
			Advance();
			ParseExpressionSegment();
			while (precedence[(int)curtok.ID] > precedence[(int)startOp])
			{
				ParseExpressionSegment();
			}
			UnaryExpression uNode = new UnaryExpression(startOp);
			uNode.Child = exprStack.pop();
			exprStack.push(uNode);
		}
		private ExpressionNode ConsumeBinary(int startOp)
		{
			ExpressionNode result = null;
			if ((exprStack.size() == 0 || precedence[(int)tokens.get(index - 2).ID] > 0))
			{
				// assert +,-,!,~,++,--,cast
				Advance();
				ParseExpressionSegment();
				while (precedence[(int)curtok.ID] > precedence[(int)startOp])
				{
					ParseExpressionSegment();
				}
				result = exprStack.pop(); // this signals it was a unary operation
			}
			else
			{
				Advance();
				BinaryExpression bNode = new BinaryExpression(startOp);
				bNode.Left = exprStack.pop();
				exprStack.push(bNode); // push node
				ParseExpressionSegment(); // right side
				// consume now or let next op consume?
				while (precedence[(int)curtok.ID] > precedence[(int)startOp])
				{
					ParseExpressionSegment();
				}
				bNode.Right = exprStack.pop();
			}
			return result;
		}
		private boolean isAfterType()
		{
			boolean result = false;
			if (exprStack.size() > 0)
			{
				if (exprStack.peek() instanceof IdentifierExpression)
				{
					IdentifierExpression ie = (IdentifierExpression)exprStack.pop();
					exprStack.push(new TypeNode(ie));
					result = true;
				}
				else if (exprStack.peek() instanceof TypeNode || exprStack.peek() instanceof MemberAccessExpression)// PrimaryExpressionNode)//
				{
					result = true;
				}
			}
			return result;
		}
		private ExpressionList ParseExpressionList(int termChar)
		{
			ExpressionList list = new ExpressionList();
			int id = curtok.ID;
			while (id != TokenID.Eof && id != termChar)
			{
				while (id != TokenID.Eof && id != termChar && id != TokenID.Comma)
				{
					ParseExpressionSegment();
					id = curtok.ID;
				}

				if (curtok.ID == TokenID.Comma)
				{
					Advance(); // over comma
				}
				list.Expressions.add(exprStack.pop());
				id = curtok.ID;
			}
			if (curtok.ID == termChar)
			{
				Advance();
			}
			return list;
		}
		private void ParseLocalDeclaration()
		{
			IdentifierExpression declIdentifier = ParseQualifiedIdentifier();
			IType type = (IType)exprStack.pop();
			LocalDeclarationStatement lnode = new LocalDeclarationStatement();
			lnode.Identifiers.add(declIdentifier);

			if (isLocalConst)
			{
				lnode.IsConstant = true;
			}
			isLocalConst = false;
			lnode.Type = type;

			// a using statement can hold a local decl without a semi, thus the rparen
			while (curtok.ID != TokenID.Eof && curtok.ID != TokenID.Semi && curtok.ID != TokenID.RParen)
			{
				while (curtok.ID == TokenID.Comma)
				{
					Advance(); // over comma
					declIdentifier = ParseQualifiedIdentifier();
					lnode.Identifiers.add(declIdentifier);
				}
				if (curtok.ID == TokenID.Equal)
				{
					Advance(); // over equal
					lnode.RightSide = ParseExpression(TokenID.Comma);

					if (curtok.ID == TokenID.Comma)
					{
						exprStack.push(lnode);
						lnode = new LocalDeclarationStatement();
						lnode.Type = type;
					}
				}
			}
			exprStack.push(lnode);
		}
		private void ParseCastOrGroup()
		{
			ExpressionNode interior = ParseExpression();
			AssertAndAdvance(TokenID.RParen);
			int rightTok = curtok.ID;

			// check if this is terminating - need better algorithm here :(
			// todo: this can probably be simplified (and correctified!) with new expression parsing style
			if (!(interior instanceof IType) ||
				rightTok == TokenID.Semi ||
				rightTok == TokenID.RParen ||
				rightTok == TokenID.RCurly ||
				rightTok == TokenID.RBracket ||
				rightTok == TokenID.Comma)
			{
				// was group for sure
				exprStack.push(new ParenthesizedExpression(interior));
				ParseContinuingPrimary();
			}
			else
			{
				// push a pe just in case upcoming is binary expr
				ParenthesizedExpression pe = new ParenthesizedExpression();
				exprStack.push(pe);

				// find out what is on right
				ParseExpressionSegment();
				ExpressionNode peek = exprStack.peek();

				if (peek instanceof PrimaryExpression || peek instanceof UnaryExpression)
				{
					// cast
					UnaryCastExpression castNode = new UnaryCastExpression();
					castNode.Type = (IType)interior;
					castNode.Child = exprStack.pop();
					// need to pop off the 'just in case' pe
					exprStack.pop();
					exprStack.push(castNode);
				}
				else
				{
					// group
					pe.Expression = interior;
					ParseContinuingPrimary();
				}
			}
		}
		private void ParseArrayCreation(TypeNode type)
		{
			ArrayCreationExpression arNode = new ArrayCreationExpression();
			exprStack.push(arNode);

			arNode.Type = type;
			int nextToken = TokenID.Invalid;
			if (tokens.size() > index)
			{
				nextToken = tokens.get(index).ID;
			}
			// this tests for literal size declarations on first rank specifiers
			if (nextToken != TokenID.Invalid && nextToken != TokenID.Comma && nextToken != TokenID.RBracket)
			{
				Advance(); // over lbracket
				arNode.RankSpecifier = ParseExpressionList(TokenID.RBracket);
			}
			// now any 'rank only' specifiers (without size decls)
			while (curtok.ID == TokenID.LBracket)
			{
				Advance(); // over lbracket
				int commaCount = 0;
				while (curtok.ID == TokenID.Comma)
				{
					commaCount++;
					Advance();
				}
				arNode.AdditionalRankSpecifiers.add(commaCount);
				AssertAndAdvance(TokenID.RBracket);
			}
			if (curtok.ID == TokenID.LCurly)
			{
				Advance();
				arNode.Initializer = new ArrayInitializerExpression();
				arNode.Initializer.Expressions = ParseExpressionList(TokenID.RCurly);
			}
		}

		private void ParseContinuingPrimary()
		{
			boolean isContinuing = curtok.ID == TokenID.LBracket || curtok.ID == TokenID.Dot || curtok.ID == TokenID.LParen;
			while (isContinuing)
			{
				switch (curtok.ID)
				{
					case TokenID.Dot:
						ParseMemberAccess();
						break;
					case TokenID.LParen:
						ParseInvocation();
						break;
					case TokenID.LBracket:
						isContinuing = ParseElementAccess();
						break;
					default:
						isContinuing = false;
						break;
				}
				if (isContinuing)
				{
					isContinuing = curtok.ID == TokenID.LBracket || curtok.ID == TokenID.Dot || curtok.ID == TokenID.LParen;
				}
			}
			// can only be one at end
			if (curtok.ID == TokenID.PlusPlus)
			{
				Advance();
				exprStack.push(new PostIncrementExpression(exprStack.pop()));
			}
			else if(curtok.ID == TokenID.MinusMinus)
			{
				Advance();
				exprStack.push(new PostDecrementExpression(exprStack.pop()));
			}
		}
		private void ParseMemberAccess()
		{
			Advance(); // over dot
			if (curtok.ID != TokenID.Ident)
			{
				ReportError("Right side of member access must be identifier");
			}
			IdentifierExpression identifier = ParseQualifiedIdentifier();
			if (exprStack.size() > 0 && exprStack.peek() instanceof IMemberAccessible)
			{
				IMemberAccessible ima = (IMemberAccessible)exprStack.pop();
				exprStack.push(new MemberAccessExpression(ima, identifier));
			}
			else
			{
				ReportError("Left side of member access must be PrimaryExpression or PredefinedType.");
			}
		}
		private void ParseInvocation()
		{
			Advance(); // over lparen

			PrimaryExpression leftSide = (PrimaryExpression)exprStack.pop();
			ExpressionList list = ParseExpressionList(TokenID.RParen);
			exprStack.push(new InvocationExpression(leftSide, list));
		}
		private boolean ParseElementAccess()
		{
			boolean isElementAccess = true;
			Advance(); // over lbracket
			ExpressionNode type = exprStack.pop(); // the caller pushed, so must have at least one element

			// case one is actaully a type decl (like T[,,]), not element access (like T[2,4])
			// in this case we need to push the type, and abort parsing the continuing
			if (curtok.ID == TokenID.Comma || curtok.ID == TokenID.RBracket)
			{
				isElementAccess = false;
				if (type instanceof IdentifierExpression)
				{
					// needs t oconvert to typeNode
					IdentifierExpression ie = (IdentifierExpression)type;
					TypeNode tp = new TypeNode(ie);
					exprStack.push(tp);
					ParseArrayRank(tp);
				}
			}
			else
			{
				// element access case
				if (type instanceof PrimaryExpression)
				{
					PrimaryExpression tp = (PrimaryExpression)type;
					ExpressionList el = ParseExpressionList(TokenID.RBracket);
					exprStack.push(new ElementAccessExpression(tp, el));
				}
				else
				{
					ReportError("Left side of Element Access must be primary expression.");
				}
			}

			return isElementAccess;
		}
		private void ParseArrayRank(TypeNode type)
		{
			// now any 'rank only' specifiers (without size decls)
			boolean firstTime = true;
			while (curtok.ID == TokenID.LBracket || firstTime)
			{
				if (!firstTime)
				{
					Advance();
				}
				firstTime = false;
				int commaCount = 0;
				while (curtok.ID == TokenID.Comma)
				{
					commaCount++;
					Advance();
				}
				type.RankSpecifiers.add(commaCount);
				AssertAndAdvance(TokenID.RBracket);
			}
		}

		// utility
		private void RecoverFromError(int id)
		{
			RecoverFromError("", id);
		}
		private void RecoverFromError(String message, int id)
		{
			String msg = "Error: Expected " + id + " found: " + curtok.ID;
			if (message != null)
				msg = message + msg;

			ReportError(msg);
			Advance();
		}
		private void ReportError(String message)
		{
            System.out.println(message + " in token " + index + " [" + curtok.ID + "]");
		}
		private void AssertAndAdvance(int id)
		{
			if (curtok.ID != id)
			{
                RecoverFromError(id);
			}
			Advance();
		}
		private void Advance()
		{
			boolean skipping = true;
			do
			{
				if (index < tokens.size())
				{
					curtok = tokens.get(index);
				}
				else
				{
					curtok = EOF;
				}

				index++;

				switch (curtok.ID)
				{
					case TokenID.SingleComment:
						break;
					case TokenID.MultiComment:
						String[] s = strings.get(curtok.Data).split("\n");
						lineCount += s.length - 1;
						break;

					case TokenID.Newline:
						lineCount++;
						break;

					case TokenID.Hash:
						// preprocessor directives
						if (!inPPDirective)
						{
							ParsePreprocessorDirective();
							if (curtok.ID != TokenID.Newline &&
								curtok.ID != TokenID.SingleComment &&
								curtok.ID != TokenID.MultiComment &&
								curtok.ID != TokenID.Hash )
							{
								skipping = false;
							}
							else if(curtok.ID == TokenID.Hash)
							{
								index--;
							}
						}
						else
						{
							skipping = false;
						}
						break;

					default:
						skipping = false;
						break;
				}
			} while (skipping);
		}


        private void SkipToEOL(int startLine)
		{
			if (lineCount > startLine)
			{
				return;
			}
			boolean skipping = true;
			do
			{
				if (index < tokens.size())
				{
					curtok = tokens.get(index);
				}
				else
				{
					curtok = EOF;
					skipping = false;
				}
				index++;

				if(curtok.ID == TokenID.Newline)
				{
					lineCount++;
					skipping = false;
				}
			} while (skipping);
		}
		private void SkipToNextHash()
		{
			boolean skipping = true;
			do
			{
				if (index < tokens.size())
				{
					curtok = tokens.get(index);
				}
				else
				{
					curtok = EOF;
					skipping = false;
				}
				index++;

				if (curtok.ID == TokenID.Hash)
				{
					skipping = false;
				}
				else if (curtok.ID == TokenID.Newline)
				{
					lineCount++;
				}
			} while (skipping);
		}
		private void SkipToElseOrEndIf()
		{
			// advance to elif, else, or endif
			int endCount = 1;
			boolean firstPassHash = curtok.ID == TokenID.Hash;
			while (endCount > 0)
			{
				if (!firstPassHash)
				{
					SkipToNextHash();
				}
				firstPassHash = false;

				if (!(index < tokens.size()))
				{
					break;
				}
				if (tokens.get(index).ID == TokenID.Ident)
				{
					String sKind = strings.get(tokens.get(index).Data);
					if (sKind .equals( "endif"))
					{
						endCount--;
					}
					else if (sKind .equals( "elif"))
					{
						if (endCount == 1)
						{
							break;
						}
					}
				}
				else if (tokens.get(index).ID == TokenID.If)
				{
					endCount++;
				}
				else if (tokens.get(index).ID == TokenID.Else)
				{
					if (endCount == 1)
					{
						break;
					}
				}
				else
				{
					break;
				}
			}
		}


//		static Parser()
//		{
//			modMap = new SortedList<TokenID, Modifier>();
//			modMap.add(TokenID.New, Modifier.New);
//			modMap.add(TokenID.Public, Modifier.Public);
//			modMap.add(TokenID.Protected, Modifier.Protected);
//			modMap.add(TokenID.Internal, Modifier.Internal);
//			modMap.add(TokenID.Private, Modifier.Private);
//			modMap.add(TokenID.Abstract, Modifier.Abstract);
//			modMap.add(TokenID.Sealed, Modifier.Sealed);
//			modMap.add(TokenID.Static, Modifier.Static);
//			modMap.add(TokenID.Virtual, Modifier.Virtual);
//			modMap.add(TokenID.Override, Modifier.Override);
//			modMap.add(TokenID.Extern, Modifier.Extern);
//			modMap.add(TokenID.Readonly, Modifier.Readonly);
//			modMap.add(TokenID.Volatile, Modifier.Volatile);
//			modMap.add(TokenID.Ref, Modifier.Ref);
//			modMap.add(TokenID.Out, Modifier.Out);
//			modMap.add(TokenID.Assembly, Modifier.Assembly);
//			modMap.add(TokenID.Field, Modifier.Field);
//			modMap.add(TokenID.Event, Modifier.Event);
//			modMap.add(TokenID.Method, Modifier.Method);
//			modMap.add(TokenID.Param, Modifier.Param);
//			modMap.add(TokenID.Property, Modifier.Property);
//			modMap.add(TokenID.Return, Modifier.Return);
//			modMap.add(TokenID.Type, Modifier.Type);
//
//			// all default to zero
//			precedence = new byte[0xFF];
//
//			// these start at 80 for no paticular reason
//			precedence[ (int)TokenID.LBracket]		= 0x90;
//
//			precedence[ (int)TokenID.LParen]		= 0x80;
//			precedence[ (int)TokenID.Star ]		 	= 0x7F;
//			precedence[ (int)TokenID.Slash ]	 	= 0x7F;
//			precedence[ (int)TokenID.Percent ]	 	= 0x7F;
//			precedence[ (int)TokenID.Plus ]		 	= 0x7E;
//			precedence[ (int)TokenID.Minus ]	 	= 0x7E;
//			precedence[ (int)TokenID.ShiftLeft ] 	= 0x7D;
//			precedence[ (int)TokenID.ShiftRight] 	= 0x7D;
//			precedence[ (int)TokenID.Less ]		 	= 0x7C;
//			precedence[ (int)TokenID.Greater ]	 	= 0x7C;
//			precedence[ (int)TokenID.LessEqual ] 	= 0x7C;
//			precedence[ (int)TokenID.GreaterEqual ]	= 0x7C;
//			precedence[ (int)TokenID.EqualEqual ]	= 0x7B;
//			precedence[ (int)TokenID.NotEqual ]	 	= 0x7B;
//			precedence[ (int)TokenID.BAnd ]		 	= 0x7A;
//			precedence[ (int)TokenID.BXor ]		 	= 0x79;
//			precedence[ (int)TokenID.BOr ]		 	= 0x78;
//			precedence[ (int)TokenID.And]			= 0x77;
//			precedence[ (int)TokenID.Or]			= 0x76;
//
//
//			preprocessor = new SortedList<String, PreprocessorID>();
//
//			preprocessor.add("define", PreprocessorID.Define);
//			preprocessor.add("undef", PreprocessorID.Undef);
//			preprocessor.add("if", PreprocessorID.If);
//			preprocessor.add("elif", PreprocessorID.Elif);
//			preprocessor.add("else", PreprocessorID.Else);
//			preprocessor.add("endif", PreprocessorID.Endif);
//			preprocessor.add("line", PreprocessorID.Line);
//			preprocessor.add("error", PreprocessorID.Error);
//			preprocessor.add("warning", PreprocessorID.Warning);
//			preprocessor.add("region", PreprocessorID.Region);
//			preprocessor.add("endregion", PreprocessorID.Endregion);
//			preprocessor.add("pragma", PreprocessorID.Pragma);
//
//		}




    }
