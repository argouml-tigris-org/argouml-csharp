package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.nodes.expressions.TypeNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:19:10 AM
 */
public class IndexerNode extends MemberNode
	{
		public TypeNode InterfaceType;


		public NodeCollection<ParamDeclNode> Params=new NodeCollection<ParamDeclNode>();

		public AccessorNode Getter;


		public AccessorNode Setter;


        public  void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			TraceModifiers(Modifiers, sb);

			Type.ToSource(sb);
			sb.append(" ");
			if (InterfaceType != null)
			{
				InterfaceType.ToSource(sb);
				sb.append(".");
			}
			sb.append("this[");
			if (Params != null)
			{
				String comma = "";
				for (ParamDeclNode pdn:Params)
				{
					sb.append(comma);
					comma = ", ";
					pdn.ToSource(sb);
				}
			}
			sb.append("]");

			// start block
			this.NewLine(sb);
			sb.append("{");
			indent++;
			this.NewLine(sb);

			if (Getter != null)
			{
				Getter.ToSource(sb);
			}
			if (Setter != null)
			{
				Setter.ToSource(sb);
			}

			indent--;
			this.NewLine(sb);
			sb.append("}");
			this.NewLine(sb);
        }
	}
