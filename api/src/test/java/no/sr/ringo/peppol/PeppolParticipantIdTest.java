package no.sr.ringo.peppol;

import eu.peppol.identifier.InvalidPeppolParticipantException;
import eu.peppol.identifier.ParticipantId;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import static eu.peppol.identifier.SchemeId.*;
import static org.testng.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.easymock.EasyMock.*;

/**
 * @author andy
 * @author thore
 */
public class PeppolParticipantIdTest {

    @Test
    public void testFoedselsnummerWhichCouldBeUsedByDigitalMultiKanal() {

        // multikanal uses fødselsnummer
        ParticipantId p1 = ParticipantId.valueOf("9999:42342342343");
        assertNotNull(p1);

        // multikanal uses orgnumbers
        assertNotNull(ParticipantId.valueOf("9999:968218743"));

    }

    @Test
    public void testParseNorwegianOrgNoTax() throws Exception {
        //Tests that parsing norweigian org num will always parse scheme NO:ORGNR
        ParticipantId no976098897MVA = ParticipantId.valueOf("NO976098897MVA");
        final ParticipantId expected = new ParticipantId(eu.peppol.identifier.SchemeId.NO_ORGNR,"976098897");
        assertEquals(no976098897MVA, expected);

        no976098897MVA = ParticipantId.valueOf("9908:976098897MVA");
        assertEquals(no976098897MVA, expected);

        no976098897MVA = ParticipantId.valueOf("NO 976098897MVA");
        assertEquals(no976098897MVA, expected);

        no976098897MVA = ParticipantId.valueOf("NO 976098897 MVA");
        assertEquals(no976098897MVA, expected);

        no976098897MVA = ParticipantId.valueOf(" NO 976098897 MVA  ");
        assertEquals(no976098897MVA, expected);
    }

    @Test
    public void testParseNorwegianOrgNoWithoutTax() throws Exception {
        //TEST THAT parsing norweigian org num will always parse scheme NO:ORGNR
        ParticipantId no976098897 = ParticipantId.valueOf("NO976098897");
        final ParticipantId expected = new ParticipantId(eu.peppol.identifier.SchemeId.NO_ORGNR,"976098897");
        assertEquals(no976098897, expected);


        no976098897 = ParticipantId.valueOf("NO 976098897");
        assertEquals(no976098897, expected);

        no976098897 = ParticipantId.valueOf("NO 976098897 ");
        assertEquals(no976098897, expected);

        no976098897 = ParticipantId.valueOf(" NO 976098897");
        assertEquals(no976098897, expected);

        no976098897 = ParticipantId.valueOf("NO 976098897");
        assertEquals(no976098897, expected);

    }

    @Test
    public void testParsePeppolParticpantId() throws Exception {

        ParticipantId no976098897 = ParticipantId.valueOf("9908:976098897");
        assertEquals(no976098897,new ParticipantId(NO_ORGNR,"976098897"));

        no976098897 = ParticipantId.valueOf("9908:976098897");
        assertEquals(no976098897,new ParticipantId(NO_ORGNR,"976098897"));

        no976098897 = ParticipantId.valueOf("9901:976098897");
        assertEquals(no976098897,new ParticipantId(DK_CPR,"976098897"));

        //invalid iso code will not be parsed.
        try {
            no976098897 = ParticipantId.valueOf("0001:976098897");
            fail("Invalid scheme should not result in a participant instance");
        } catch (Exception e) {

        }

    }

    @Test
    public void testIsValid() {

        // a valid orgNo
        assertFalse(ParticipantId.isValidParticipantIdentifier("968218743"));

        // not valid
        assertFalse(ParticipantId.isValidParticipantIdentifier("123456789"));

        // null
        assertFalse(ParticipantId.isValidParticipantIdentifier((String) null));

        // empty String
        assertFalse(ParticipantId.isValidParticipantIdentifier(""));

        // Only organisation number, impossible to determine the scheme
        assertFalse(ParticipantId.isValidParticipantIdentifier("961329310"));


    }

    /**
     * Tests that when using value of we get null with invalid norwegian organisation numbers
     */
    @Test()
    public void testIsValidValueOf() {


        assertNotNull(ParticipantId.valueOf("9908:968218743"));

        assertNotNull(ParticipantId.valueOf("9908:NO976098897MVA"));

        assertNotNull(ParticipantId.valueOf("9908:NO 976098897 MVA"));

        assertNotNull(ParticipantId.valueOf("9908:976098897 MVA"));

        assertNotNull(ParticipantId.valueOf("9908:976098897MVA"));


    }

    @Test(expectedExceptions = {InvalidPeppolParticipantException.class})
    public void invalidOrganisationNumbers() {
        // Not a valid orgNo
        assertNotNull(ParticipantId.valueOf("968218743"));

        // not valid
        assertNull(ParticipantId.valueOf("123456789"));
        assertNull(ParticipantId.valueOf("986532933"));
        assertNull(ParticipantId.valueOf("986532952"));
        assertNull(ParticipantId.valueOf("986532954"));
        assertNull(ParticipantId.valueOf("986532955"));
        assertNotNull(ParticipantId.valueOf("988890081"));

        assertNotNull(ParticipantId.valueOf("968 218 743"));

        // null
        assertNull(ParticipantId.valueOf((String) null));

        // empty String
        assertNull(ParticipantId.valueOf(""));
    }

    @Test
    public void testOrganistaionId() throws Exception {
        ParticipantId peppolParticipantId = ParticipantId.valueOf("9908:968218743");
    }

    @Test
    public void testOrgNumWithSpaces() throws Exception {
        ParticipantId organisationNumber = ParticipantId.valueOf("9908:968 218 743");

        organisationNumber = ParticipantId.valueOf("99 08:9682 18743");

        organisationNumber = ParticipantId.valueOf("00 07:9682 18743");
    }


    @Test
    public void testTooLongOrgNo() {
        try{
            ParticipantId orgNo = new ParticipantId(NO_ORGNR, "1234567890123456789012345678901234567890");
            fail();
        } catch (InvalidPeppolParticipantException e){
            // As Expected
        }
    }

    @Test
    public void testSerialize() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        ObjectInputStream ois = null;
        try {
            final ParticipantId expectedParticipantId = ParticipantId.valueOf("9908:976098897");

            oos.writeObject(expectedParticipantId);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ois = new ObjectInputStream(in);

            final ParticipantId peppolParticipantId = (ParticipantId) ois.readObject();
            assertEquals(peppolParticipantId, expectedParticipantId);
        }
        finally {
            oos.close();
            if (ois != null) {
                ois.close();
            }
        }


        assertTrue(out.toByteArray().length > 0);
    }




    @Test(enabled = false)
    public void testSRO3079() throws Exception {

        ParticipantId peppolParticipantId = ParticipantId.valueOf("9147:91723");
        assertNotNull(peppolParticipantId);

        peppolParticipantId = ParticipantId.valueOf("9957:61394");
        assertNotNull(peppolParticipantId);
    }

    @Test
    public void formatNorwegianOrgno() {
        ParticipantId ppid = ParticipantId.valueOf("NO976098897MVA");

    }
}
