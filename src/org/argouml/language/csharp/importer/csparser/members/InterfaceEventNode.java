package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:24:43 AM
 */
public class InterfaceEventNode extends MemberNode
	{
		public void ToSource(StringBuilder sb)
		{
			if (Attributes != null)
			{
				Attributes.ToSource(sb);
				this.NewLine(sb);
			}
			this.TraceModifiers(this.Modifiers, sb);

			sb.append("event ");
			this.Type.ToSource(sb);

			sb.append(" ");
			this.Names.get(0).ToSource(sb);

			sb.append(";");
		}
	}