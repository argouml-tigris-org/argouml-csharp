package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.collections.NodeCollection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 18, 2008
 * Time: 11:25:22 AM
 */
public class InterfaceIndexerNode extends MemberNode
	{
		public NodeCollection<ParamDeclNode> Params =new NodeCollection<ParamDeclNode>();


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
			sb.append("this [");
			if (Params != null)
			{
				Params.ToSource(sb);
			}
			sb.append("]{");
			if (HasGetter)
			{
				sb.append("get;");
			}
			if (HasSetter)
			{
				sb.append("set;");
			}
			sb.append("}");
		}
	}
