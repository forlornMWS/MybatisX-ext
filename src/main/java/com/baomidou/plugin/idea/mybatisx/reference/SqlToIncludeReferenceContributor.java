package com.baomidou.plugin.idea.mybatisx.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class SqlToIncludeReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // 注册对 <sql id="..."> 中 id 属性的引用提供器
        registrar.registerReferenceProvider(
            XmlPatterns.xmlAttributeValue()
                .withParent(XmlPatterns.xmlAttribute("id")
                    .withParent(XmlPatterns.xmlTag().withName("sql"))),
            new PsiReferenceProvider() {
                @NotNull
                @Override
                public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                             @NotNull ProcessingContext context) {
                    XmlAttributeValue attributeValue = (XmlAttributeValue) element;
                    String sqlId = attributeValue.getValue();

                    if (sqlId.isEmpty()) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    XmlAttribute attribute = (XmlAttribute) attributeValue.getParent();
                    XmlTag sqlTag = attribute.getParent();

                    Collection<XmlAttributeValue> refIdValues = findIncludeRefIdValues(sqlTag, sqlId);

                    if (refIdValues.isEmpty()) {
                        return PsiReference.EMPTY_ARRAY;
                    }

                    // 为每个引用创建一个 PsiReference
                    List<PsiReference> references = new ArrayList<>();

                    // 整个文本范围，排除引号
                    TextRange range = new TextRange(1, attributeValue.getTextLength() - 1);

                    references.add(new SqlIdReference(attributeValue, range, refIdValues));

                    return references.toArray(new PsiReference[0]);
                }
            }
        );
    }

    private Collection<XmlAttributeValue> findIncludeRefIdValues(XmlTag sqlTag, String sqlId) {
        Collection<XmlAttributeValue> results = new ArrayList<>();

        // 在当前文件中查找
        if (sqlTag.getContainingFile() != null) {
            // 找到同一文件中的所有 include 标签
            Collection<XmlTag> allTags = PsiTreeUtil.findChildrenOfType(sqlTag.getContainingFile(), XmlTag.class);

            for (XmlTag xmlTag : allTags) {
                if ("include".equals(xmlTag.getName())) {
                    XmlAttribute refIdAttr = xmlTag.getAttribute("refid");
                    if (refIdAttr != null) {
                        XmlAttributeValue refidValue = refIdAttr.getValueElement();
                        if (refidValue != null) {
                            String refId = refidValue.getValue();
                            if (refId.equals(sqlId) || refId.endsWith("." + sqlId)) {
                                results.add(refidValue);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    // 自定义引用实现
    private static class SqlIdReference extends PsiReferenceBase<XmlAttributeValue> implements PsiPolyVariantReference {
        private final Collection<XmlAttributeValue> refIdValues;

        public SqlIdReference(XmlAttributeValue element, TextRange range, Collection<XmlAttributeValue> refIdValues) {
            super(element, range);
            this.refIdValues = refIdValues;
        }

        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            List<ResolveResult> results = new ArrayList<>();

            for (XmlAttributeValue refIdValue : refIdValues) {
                results.add(new PsiElementResolveResult(refIdValue));
            }

            return results.toArray(new ResolveResult[0]);
        }

        @Override
        public PsiElement resolve() {
            ResolveResult[] resolveResults = multiResolve(false);
            return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
        }

        @Override
        public @NotNull String getCanonicalText() {
            // Use the PSI elements to get a proper name
            return super.getCanonicalText();
        }

        @Override
        public PsiElement handleElementRename(@NotNull String newElementName) {
            // Add logic to rename the reference in the source document.
            return super.handleElementRename(newElementName);
        }

        @Override
        public @NotNull TextRange getRangeInElement() {
            // Determine the range of the reference
            return new TextRange(0, myElement.getTextLength());
        }

    }

}
