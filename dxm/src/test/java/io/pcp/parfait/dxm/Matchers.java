package io.pcp.parfait.dxm;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.ReflectionComparatorMode;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.report.impl.DefaultDifferenceReport;

import static org.unitils.reflectionassert.ReflectionComparatorFactory.createRefectionComparator;

public class Matchers {

    static class ReflectiveMatcher extends TypeSafeMatcher<Object> {

        private final Object expected;
        private Difference difference;

        private ReflectiveMatcher(Object expected) {
            this.expected = expected;
        }

        @Override
        protected boolean matchesSafely(Object actual) {
            ReflectionComparator refectionComparator = createRefectionComparator(ReflectionComparatorMode.LENIENT_ORDER);
            difference = refectionComparator.getDifference(expected, actual);
            return refectionComparator.isEqual(expected, actual);
        }

        @Override
        public void describeTo(Description description) {
            if(difference != null) {
                description.appendText(new DefaultDifferenceReport().createReport(difference));
            }
        }

        static ReflectiveMatcher reflectivelyEqualing(Object expected) {
            return new ReflectiveMatcher(expected);
        }

    }

}
