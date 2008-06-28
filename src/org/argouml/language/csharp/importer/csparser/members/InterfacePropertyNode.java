package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:30:32 AM
 */
public class InterfacePropertyNode extends MemberNode
	{
		public boolean HasGetter;

		public boolean HasSetter;


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

			this.Names.get(0).ToSource(sb);

			// start block
			this.NewLine(sb);
			sb.append("{");

			if (HasGetter)
			{
				sb.append("get; ");
			}
			if (HasSetter)
			{
				sb.append("set; ");
			}

			sb.append("}");
			this.NewLine(sb);
		}
	}
