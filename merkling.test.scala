package scalus.merkle_patricia_forestry

import munit.ScalaCheckSuite
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.*
import scalus.*
import scalus.builtin.given
import scalus.builtin.Builtins.*
import scalus.builtin.ByteString
import scalus.builtin.ByteString.hex
import scalus.merkle_patricia_forestry.Merkling.*
import scalus.uplc.eval.PlutusVM

class MerklingTest extends ScalaCheckSuite:
    // Custom generator 32 random bytes ByteString
    private val byteArrayGen: Gen[ByteString] =
        Gen.containerOfN[Array, Byte](32, Arbitrary.arbByte.arbitrary).map(ByteString.fromArray)

    private given PlutusVM = PlutusVM.makePlutusV3VM()

    test("combine generates expected hashes for null sequences") {
        val sir = Compiler.compile(combine(NullHash, NullHash))
        println(sir.showHighlighted)
        println(sir.toUplc().evaluateDebug)
        assertEquals(combine(NullHash, NullHash), NullHash2)
        assertEquals(combine(NullHash2, NullHash2), NullHash4)
        assertEquals(combine(NullHash4, NullHash4), NullHash8)
    }

    test("suffix generates expected values for specific examples") {
        assertEquals(suffix(hex"abcd456789", 0), hex"ffabcd456789")
        assertEquals(suffix(hex"abcd456789", 1), hex"000bcd456789")
        assertEquals(suffix(hex"abcd456789", 2), hex"ffcd456789")
        assertEquals(suffix(hex"abcd456789", 4), hex"ff456789")
        assertEquals(suffix(hex"abcd456789", 5), hex"00056789")
        assertEquals(suffix(hex"abcd456789", 10), hex"ff")
    }

    property("suffix always starts with either 0x00 or 0xff") {
        forAll(byteArrayGen, Gen.choose(0, 31)) { (bytes, index) =>
            val result = suffix(bytes, index)
            val head = indexByteString(result, 0)

            if head == 0 then indexByteString(result, 1) < 16
            else head == 255
        }
    }

    test("nibbles extracts correct sequences for specific examples") {
        assertEquals(nibbles(hex"0123456789", 2, 2), ByteString.empty)
        assertEquals(nibbles(hex"0123456789", 2, 3), hex"02")
        assertEquals(nibbles(hex"0123456789", 4, 8), hex"04050607")
        assertEquals(nibbles(hex"0123456789", 3, 6), hex"030405")
        assertEquals(nibbles(hex"0123456789", 1, 7), hex"010203040506")
    }

    test("nibble extracts correct values for specific examples") {
        assertEquals(nibble(hex"ab", 0), BigInt(10)) // 'a' high nibble
        assertEquals(nibble(hex"ab", 1), BigInt(11)) // 'a' low nibble
    }

    property("nearby nibbles combine to form original byte") {
        forAll(byteArrayGen, Gen.choose(0, 31)) { (bytes, index) =>
            val msb = nibble(bytes, index * 2)
            val lsb = nibble(bytes, index * 2 + 1)

            msb * 16 + lsb == indexByteString(bytes, index)
        }
    }

    property("nibble always returns values between 0 and 15") {
        forAll(byteArrayGen, Gen.choose(0, 63)) { (bytes, index) =>
            val digit = nibble(bytes, index)
            digit >= 0 && digit <= 15
        }
    }

    property("merkle4 constructs correct root hash for all positions") {
        forAll(byteArrayGen, byteArrayGen, byteArrayGen, byteArrayGen) { (a, b, c, d) =>
            // Calculate expected root hash
            val root = combine(combine(a, b), combine(c, d))

            // Test all positions
            val test0 = merkle4(0, a, combine(c, d), b) == root
            val test1 = merkle4(1, b, combine(c, d), a) == root
            val test2 = merkle4(2, c, combine(a, b), d) == root
            val test3 = merkle4(3, d, combine(a, b), c) == root

            // All conditions must be true
            test0 && test1 && test2 && test3
        }
    }
