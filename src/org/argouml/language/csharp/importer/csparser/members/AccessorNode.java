package org.argouml.language.csharp.importer.csparser.members;

import org.argouml.language.csharp.importer.csparser.statements.BlockStatement;

/**
 * Created by IntelliJ IDEA.
 * User: Thilina
 * Date: Jun 7, 2008
 * Time: 7:25:37 PM
 */
public class AccessorNode extends MemberNode {
    public String Kind;


    public boolean IsAbstractOrInterface = false;

    public BlockStatement StatementBlock = new BlockStatement();


    public void ToSource(StringBuilder sb) {
        if (Attributes != null) {
            Attributes.ToSource(sb);
            this.NewLine(sb);
        }
        sb.append(Kind);
        if (IsAbstractOrInterface) {
            sb.append(";");
        } else {
            this.NewLine(sb);
            // statements
            this.StatementBlock.ToSource(sb);
        }
    }

}
