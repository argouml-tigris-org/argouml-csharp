package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.nodes.expressions.ArgumentNode;
import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;
import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 8, 2008
 * Time: 11:10:45 PM
 */
public class ConstructorNode extends MemberNode
	{
		public boolean HasThis;


		public boolean HasBase;


		public NodeCollection<ArgumentNode> ThisBaseArgs;

		public NodeCollection<ParamDeclNode> Params;

		public BlockStatement StatementBlock = new BlockStatement();


		public boolean IsStaticConstructor = false;


        public  void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.Modifiers, sb);

			if (IsStaticConstructor)
			{
				sb.append("static ");
			}

			this.Names.get(0).ToSource(sb);
			sb.append("(");

			String comma = "";
			if (Params != null)
			{
				for (int i = 0; i < Params.size(); i++)
				{
					sb.append(comma);
					comma = ", ";
					Params.get(i).ToSource(sb);
				}
			}
			sb.append(")");

			// possible :this or :base
			if (HasBase)
			{
				sb.append(" : base(");
			}
			else if (HasThis)
			{
				sb.append(" : this(");
			}
			if (HasBase || HasThis)
			{
				if (ThisBaseArgs != null)
				{
					comma = "";
					for (int i = 0; i < ThisBaseArgs.size(); i++)
					{
						sb.append(comma);
						comma = ", ";
						ThisBaseArgs.get(i).ToSource(sb);
					}
				}
				sb.append(")");
			}

			// start block
			this.NewLine(sb);

			StatementBlock.ToSource(sb);

        }

	}
