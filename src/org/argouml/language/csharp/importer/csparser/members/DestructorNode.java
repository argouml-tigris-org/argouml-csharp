package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:07:51 AM
 */
public class DestructorNode extends MemberNode
	{
		public BlockStatement StatementBlock = new BlockStatement();


		public void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.Modifiers, sb);

			sb.append("~");
			this.Names.get(0).ToSource(sb);
			sb.append("()");
			this.NewLine(sb);

			StatementBlock.ToSource(sb);
		}

	}
