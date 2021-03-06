/* SortKey.java -- 
   Copyright (C) 2004 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package gnu.xml.transform;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import gnu.xml.xpath.Expr;

/**
 * <p>
 * An XSL sort key, as specified by section 10 of the XSL
 * Transformations specification.  This takes the form:
 * </p>
 * <pre>
 * &lt;xsl:sort
 * select = string-expression
 * lang = { nmtoken }
 * data-type = { "text" | "number" | qname-but-not-ncname }
 * order = { "ascending" | "descending" }
 * case-order = { "upper-first" | "lower-first" } /&rt;
 * </pre>
 * <p>
 * Note that all but the selection expression are optional,
 * and so may be {@code null}.
 * </p>
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
final class SortKey
{

  static final int DEFAULT = 0;
  static final int UPPER_FIRST = 1;
  static final int LOWER_FIRST = 2;

  final Expr select;
  final TemplateNode langTemplate;
  final TemplateNode dataTypeTemplate;
  final TemplateNode orderTemplate;
  final TemplateNode caseOrderTemplate;

  transient String lang;
  transient String dataType;
  transient boolean descending;
  transient int caseOrder;

  /**
   * Constructs a new {@link SortKey} to represent an &lt;xsl:sort&rt;
   * tag.
   *
   * @param select the XPath expression which selects the nodes to be sorted.
   * @param lang the language of the sort keys or {@code null} if unspecified.
   * @param dataType the data type of the strings.  May be "string", "number",
   *                 a QName or {@code null} if unspecified.
   * @param order the ordering of the nodes, which may be "ascending", "descending"
   *              or {@code null} if unspecified.
   * @param caseOrder the treatment of case when the data type is a string.  This
   *                  may be "upper-first", "lower-first" or {@code null} if
   *                  unspecified.
   */ 
  SortKey(Expr select, TemplateNode lang, TemplateNode dataType,
          TemplateNode order, TemplateNode caseOrder)
  {
    this.select = select;
    this.langTemplate = lang;
    this.dataTypeTemplate = dataType;
    this.orderTemplate = order;
    this.caseOrderTemplate = caseOrder;
  }

  String key(Node node)
  {
    Object ret = select.evaluate(node, 1, 1);
    if (ret instanceof String)
      {
        return (String) ret;
      }
    else
      {
        return Expr._string(node, ret);
      }
  }

  /**
   * Prepare for a sort.
   * This sets all transient variables from their AVTs.
   */
  void init(Stylesheet stylesheet, QName mode,
            Node context, int pos, int len,
            Node parent, Node nextSibling)
    throws TransformerException
  {
    Document doc = (context instanceof Document) ? (Document) context :
      context.getOwnerDocument();
    if (langTemplate == null)
      {
        lang = null;
      }
    else
      {
        DocumentFragment fragment = doc.createDocumentFragment();
        langTemplate.apply(stylesheet, mode, context, pos, len,
                           fragment, null);
        lang = Expr.stringValue(fragment);
      }
    if (dataTypeTemplate == null)
      {
        dataType = "text";
      }
    else
      {
        DocumentFragment fragment = doc.createDocumentFragment();
        dataTypeTemplate.apply(stylesheet, mode, context, pos, len,
                               fragment, null);
        dataType = Expr.stringValue(fragment);
      }
    if (orderTemplate == null)
      {
        descending = false;
      }
    else
      {
        DocumentFragment fragment = doc.createDocumentFragment();
        orderTemplate.apply(stylesheet, mode, context, pos, len,
                            fragment, null);
        String order = Expr.stringValue(fragment);
        descending = "descending".equals(order);
      }
    if (caseOrderTemplate == null)
      {
        caseOrder = DEFAULT;
      }
    else
      {
        DocumentFragment fragment = doc.createDocumentFragment();
        caseOrderTemplate.apply(stylesheet, mode, context, pos, len,
                                fragment, null);
        String co = Expr.stringValue(fragment);
        caseOrder = "upper-first".equals(co) ? UPPER_FIRST :
          "lower-first".equals(co) ? LOWER_FIRST :
          DEFAULT;
      }
  }

  boolean references(QName var)
  {
    if (select != null && select.references(var))
      {
        return true;
      }
    if (langTemplate != null && langTemplate.references(var))
      {
        return true;
      }
    if (dataTypeTemplate != null && dataTypeTemplate.references(var))
      {
        return true;
      }
    if (orderTemplate != null && orderTemplate.references(var))
      {
        return true;
      }
    if (caseOrderTemplate != null && caseOrderTemplate.references(var))
      {
        return true;
      }
    return false;
  }

  /**
   * Provides a clone of this {@link SortKey}, using the given
   * stylesheet as a context.
   *
   * @param stylesheet the stylesheet which provides context for the cloning.
   * @return a clone of this instance.
   */
  SortKey clone(Stylesheet stylesheet)
  {
    return new SortKey(select.clone(stylesheet),
		       langTemplate == null ? null : cloneAttributeValueTemplate(langTemplate, stylesheet),
		       dataTypeTemplate == null ? null : cloneAttributeValueTemplate(dataTypeTemplate, stylesheet),
		       orderTemplate == null ? null : cloneAttributeValueTemplate(orderTemplate, stylesheet),
		       caseOrderTemplate == null ? null : cloneAttributeValueTemplate(caseOrderTemplate, stylesheet));
  }

  /**
   * Clones an attribute value template as created by
   * {@link Stylesheet#parseAttributeValueTemplate(String, Node)}.
   * The node may either by a literal node or an xsl:value-of expression.
   *
   * @param node the node to clone.
   * @param stylesheet the stylesheet which provides context for the cloning.
   * @return the cloned node.
   */
  private TemplateNode cloneAttributeValueTemplate(TemplateNode node, Stylesheet stylesheet)
  {
    if (node instanceof ValueOfNode)
      return ((ValueOfNode) node).clone(stylesheet);
    return ((LiteralNode) node).clone(stylesheet);
  }
}
