package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.enums.TokenID;
import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:34:28 AM
 */
public class OperatorNode extends MemberNode
	{
		public OperatorNode()
		{
		}

		public int Operator;


		public boolean IsExplicit;

		public boolean IsImplicit;


		public ParamDeclNode Param1;

		public ParamDeclNode Param2;


		public BlockStatement Statements = new BlockStatement();


        public void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.Modifiers, sb);

			if (IsExplicit)
			{
				sb.append("explicit operator ");
				Type.ToSource(sb);
			}
			else if (IsImplicit)
			{
				sb.append("implicit operator ");
				Type.ToSource(sb);
			}
			else
			{
				Type.ToSource(sb);
				sb.append("operator " + Operator + " ");
			}

			sb.append("(");
			if (Param1 != null)
			{
				Param1.ToSource(sb);
			}
			if (Param2 != null)
			{
				sb.append(", ");
				Param2.ToSource(sb);
			}
			sb.append(")");
			this.NewLine(sb);

			if (Statements != null)
			{
				Statements.ToSource(sb);
			}
			else
			{
				sb.append("{}");
			}

        }
	}