package org.argouml.language.csharp.importer.csparser.members;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:37:27 AM
 */
public class PropertyNode extends MemberNode
	{
		public AccessorNode Getter;


		public AccessorNode Setter;

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