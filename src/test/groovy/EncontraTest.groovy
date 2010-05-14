import groovy.util.GroovyTestCase

class EncontraTest extends GroovyTestCase {
    void testResult(){
        def result = "foo!"
        assert "foo!" == result : "foo! is not the result!"
    }
}
