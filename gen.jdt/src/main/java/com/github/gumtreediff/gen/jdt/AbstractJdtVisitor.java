/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.gen.jdt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.gen.jdt.cd.EntityType;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public abstract class AbstractJdtVisitor extends ASTVisitor {

    protected TreeContext context = new TreeContext();

    private Deque<ITree> trees = new ArrayDeque<>();

    public AbstractJdtVisitor() {
        super(true);
    }

    public TreeContext getTreeContext() {
        return context;
    }

    protected void pushNode(ASTNode n, String label) {
        int type = n.getNodeType();
        String typeName = n.getClass().getSimpleName();
        String propertyId = null;
        StructuralPropertyDescriptor propertyDesc = n.getLocationInParent();
        if (propertyDesc != null) {
        	propertyId = propertyDesc.getId();
        }
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = n.properties();
		@SuppressWarnings("unchecked")
		List<StructuralPropertyDescriptor> structuralProperties = n.structuralPropertiesForType();
		Map<SimplePropertyDescriptor, Object> simpleProperties = new HashMap<SimplePropertyDescriptor, Object>();
		for (StructuralPropertyDescriptor prop :structuralProperties) {
			if (prop.isSimpleProperty()) {
				simpleProperties.put((SimplePropertyDescriptor) prop, n.getStructuralProperty(prop));
			}
		}
        
        push(type, typeName, label, n.getStartPosition(), n.getLength(), propertyId, properties, simpleProperties);
    }

	protected void pushFakeNode(EntityType n, int startPosition, int length) {
        int type = -n.ordinal(); // Fake types have negative types (but does it matter ?)
        String typeName = n.name();
        push(type, typeName, "", startPosition, length, "", new HashMap<String, Object>(), null);
    }

    private void push(int type, String typeName, String label, int startPosition, int length, String propertyId, Map<String, Object> properties, Map<SimplePropertyDescriptor, Object> simpleProperties) {
        ITree t = context.createTree(type, label, typeName);
        t.setPos(startPosition);
        t.setLength(length);
        t.setMetadata("propertyId", propertyId);
        t.setMetadata("properties", properties);
        t.setMetadata("simpleProperties", simpleProperties);
        
        if (trees.isEmpty())
            context.setRoot(t);
        else {
            ITree parent = trees.peek();
            t.setParentAndUpdateChildren(parent);
        }

        trees.push(t);
    }

    protected ITree getCurrentParent() {
        return trees.peek();
    }

    protected void popNode() {
        trees.pop();
    }
}
