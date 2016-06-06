/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.ruleunit;

import org.drools.ruleunit.reactive.ReactiveCollection;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;

import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

public class RuleUnitTest {

    @Test
    public void test() {
        UnitAwareKieSession ksession = createUnitAwareKieSession();

        List<Person> persons = asList( new Person( "Mario", 42 ),
                                       new Person( "Marilena", 44 ),
                                       new Person( "Sofia", 4 ));

        System.out.println( "--- Executing " + MyRuleUnit.class.getSimpleName() );
        ksession.exec( MyRuleUnit.class, persons );

        System.out.println( "--- Executing " + AnotherRuleUnit.class.getSimpleName() );
        ksession.exec( AnotherRuleUnit.class, persons );
    }

    @Test
    public void testReactive() throws Exception {
        UnitAwareKieSession ksession = createUnitAwareKieSession();

        Collection<Person> persons = new ReactiveCollection<>();
        ksession.execUntilHalt( ReactiveRuleUnit.class, persons );

        persons.add( new Person( "Mario", 42 ) );
        Thread.sleep(1000L);
        persons.add( new Person( "Sofia", 4 ) );
        Thread.sleep(1000L);
        persons.add( new Person( "Marilena", 44 ) );
        Thread.sleep(1000L);

        ksession.halt();
    }

    private UnitAwareKieSession createUnitAwareKieSession() {
        KieServices ks = KieServices.Factory.get();
        KieContainer kc = ks.newKieClasspathContainer();

        UnitAwareKieBase kbase = new UnitAwareKieBase( kc.getKieBase() );
        return kbase.newKieSession();
    }

}
