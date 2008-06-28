package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 8, 2008
 * Time: 11:07:47 PM
 */
public class ConstantNode extends MemberNode
    {
        public void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.Modifiers, sb);
			sb.append("const ");

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
