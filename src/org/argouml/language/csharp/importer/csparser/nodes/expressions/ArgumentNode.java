package org.argouml.language.csharp.importer.csparser.nodes.expressions;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 6:15:29 PM
 */
public class ArgumentNode extends BaseNode
	{
		public Boolean IsRef;


		public Boolean IsOut;
		

		public ExpressionNode Expression;

        public void ToSource(StringBuilder sb)
		{
			if (IsRef)
			{
				sb.append("ref ");
			}
			else if (IsOut)
			{
				sb.append("out ");
			}

			Expression.ToSource(sb);

        }


	}
