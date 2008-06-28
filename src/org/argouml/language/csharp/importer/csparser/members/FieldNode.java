package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:17:51 AM
 */
public class FieldNode extends MemberNode
	{
        public void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.Modifiers, sb);

			this.Type.ToSource(sb);
			sb.append(" ");

			String comma = "";
			for (int i = 0; i < this.Names.size(); i++)
			{
				sb.append(comma);
				comma = ", ";
				this.Names.get(i).ToSource(sb);
			}

			if (this.Value != null)
			{
				sb.append(" = ");
				this.Value.ToSource(sb);
			}

			sb.append(";");
			this.NewLine(sb);
        }
	}
