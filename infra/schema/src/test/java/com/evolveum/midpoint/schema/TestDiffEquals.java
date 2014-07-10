/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.schema;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.schema.constants.MidPointConstants;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author lazyman
 */
public class TestDiffEquals {

    @BeforeSuite
    public void setup() throws SchemaException, SAXException, IOException {
        PrettyPrinter.setDefaultNamespacePrefix(MidPointConstants.NS_MIDPOINT_PUBLIC_PREFIX);
        PrismTestUtil.resetPrismContext(MidPointPrismContextFactory.FACTORY);
    }

    @Test
    public void testUserSimplePropertyDiff() throws SchemaException {
        UserType userType1 = new UserType();
        userType1.setName(PrismTestUtil.createPolyStringType("test name"));
        UserType userType2 = new UserType();
        userType2.setName(PrismTestUtil.createPolyStringType("test name"));
        PrismTestUtil.getPrismContext().adopt(userType1);
        PrismTestUtil.getPrismContext().adopt(userType2);

        ObjectDelta delta = userType1.asPrismObject().diff(userType2.asPrismObject());
        assertNotNull(delta);
        assertEquals(0, delta.getModifications().size());

        userType2.setDescription(null);

        delta = userType1.asPrismObject().diff(userType2.asPrismObject());
        assertNotNull(delta);
        assertEquals("Delta should be empty, nothing changed.", 0, delta.getModifications().size());
    }

    @Test
    public void testUserListSimpleDiff() throws SchemaException {
        UserType u1 = new UserType();
        u1.setName(PrismTestUtil.createPolyStringType("test name"));
        UserType u2 = new UserType();
        u2.setName(PrismTestUtil.createPolyStringType("test name"));
        PrismTestUtil.getPrismContext().adopt(u1);
        PrismTestUtil.getPrismContext().adopt(u2);

        ObjectDelta delta = u1.asPrismObject().diff(u2.asPrismObject());
        assertNotNull(delta);
        assertEquals(0, delta.getModifications().size());

        u2.getAdditionalName();

        delta = u1.asPrismObject().diff(u2.asPrismObject());
        assertNotNull(delta);
        assertEquals("Delta should be empty, nothing changed.", 0, delta.getModifications().size());
    }

    @Test
    public void testContextlessEquals() throws Exception {
        AssignmentType a1 = new AssignmentType();            // no prismContext here
        a1.setDescription("descr1");

        AssignmentType a2 = new AssignmentType();            // no prismContext here
        a2.setDescription("descr2");

        // assertFalse(a1.equals(a2));                       // this DOES NOT work without prismContext

        PrismContext prismContext = PrismTestUtil.getPrismContext();
        prismContext.adopt(a1);
        prismContext.adopt(a2);
        assertFalse(a1.equals(a2));                         // now it should work
    }

    @Test
    public void testContextlessEquals2() throws Exception {

        // (1) user without prismContext - the functionality is radically reduced

        UserType user = new UserType();

        // user.asPrismObject().createDelta();               // this fails (no prismContext)

        AssignmentType a1 = new AssignmentType();            // no prismContext here
        a1.setDescription("descr1");
        user.getAssignment().add(a1);
        AssignmentType a2 = new AssignmentType();            // no prismContext here
        a2.setDescription("descr2");
        //user.getAssignment().add(a2);                      // this fails without prismContext (there's hidden equals there)

        // (2) user with prismContext

        UserType userWithContext = new UserType();
        PrismContext prismContext = PrismTestUtil.getPrismContext();
        prismContext.adopt(userWithContext);

        AssignmentType b1 = new AssignmentType();            // no prismContext here
        b1.setDescription("descr1");
        userWithContext.getAssignment().add(b1);
        AssignmentType b2 = new AssignmentType();            // no prismContext here
        b2.setDescription("descr2");
        userWithContext.getAssignment().add(b2);             // this works, because there's already prismContext in userWithContext

        // b1 and b2 obtain context when they are added to the container
        assertNotNull(b1.asPrismContainerValue().getPrismContext());
        assertNotNull(b2.asPrismContainerValue().getPrismContext());
        assertFalse(b1.equals(b2));

        userWithContext.asPrismObject().createDelta();       // this works as well
    }

}
