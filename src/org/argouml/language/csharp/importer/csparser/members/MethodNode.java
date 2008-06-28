package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:32:31 AM
 */
public class MethodNode extends MemberNode
	{
		public NodeCollection<ParamDeclNode> Params =new NodeCollection<ParamDeclNode>();


		public BlockStatement StatementBlock = new BlockStatement();


		public  void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.Modifiers, sb);

			this.Type.ToSource(sb);
			sb.append(" ");

			this.Names.get(0).ToSource(sb);
			sb.append("(");

			if (Params != null)
			{
				String comma = "";
				for (int i = 0; i < Params.size(); i++)
				{
					sb.append(comma);
					comma = ", ";
					Params.get(i).ToSource(sb);
				}
			}

			sb.append(")");
			this.NewLine(sb);

			StatementBlock.ToSource(sb);
		}
	}