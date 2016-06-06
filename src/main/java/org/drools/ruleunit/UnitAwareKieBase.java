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

import org.drools.core.definitions.rule.impl.RuleImpl;
import org.kie.api.KieBase;

public class UnitAwareKieBase {
    private final KieBase kbase;

    public UnitAwareKieBase( KieBase kbase ) {
        this.kbase = kbase;
        initAgendaGroupOnRules();
    }

    private void initAgendaGroupOnRules() {
        kbase.getKiePackages().stream()
                .flatMap( pkg -> pkg.getRules().stream() )
                .forEach( r -> {
                    RuleImpl rule = (RuleImpl) r;
                    String unitName = sourcePathToUnit(rule.getResource().getSourcePath());
                    rule.setAgendaGroup( unitName );
                });
    }

    private static String sourcePathToUnit(String sourcePath) {
        int lastSep = sourcePath.lastIndexOf( '/' );
        String unit = lastSep > 0 ? sourcePath.substring( lastSep+1 ) : sourcePath;
        int lastDot = unit.lastIndexOf( '.' );
        return lastDot > 0 ? unit.substring( 0, lastDot ) : unit;
    }

    public UnitAwareKieSession newKieSession() {
        return new UnitAwareKieSession( kbase.newKieSession() );
    }
}
