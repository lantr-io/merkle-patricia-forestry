package scalus.merkle_patricia_forestry
import org.openjdk.jmh.annotations.*
import scalus.builtin.ByteString
import scalus.builtin.ByteString.hex
import scalus.merkle_patricia_forestry.MerklePatriciaForestry.ProofStep
import scalus.merkle_patricia_forestry.MerklePatriciaForestry.ProofStep.Branch
import scalus.prelude.List

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
class MpfBenchmark {

    val proofBitcoin845602: List[ProofStep] = List.Cons(
      Branch(
        skip = 0,
        neighbors =
            hex"bc13df27a19f8caf0bf922c900424025282a892ba8577095fd35256c9d553ca120b8645121ebc9057f7b28fa4c0032b1f49e616dfb8dbd88e4bffd7c0844d29b011b1af0993ac88158342583053094590c66847acd7890c86f6de0fde0f7ae2479eafca17f9659f252fa13ee353c879373a65ca371093525cf359fae1704cf4a"
      ),
      List.Cons(
        Branch(
          skip = 0,
          neighbors =
              hex"255753863960985679b4e752d4b133322ff567d210ffbb10ee56e51177db057460b547fe42c6f44dfef8b3ecee35dfd4aa105d28b94778a3f1bb8211cf2679d7434b40848aebdd6565b59efdc781ffb5ca8a9f2b29f95a47d0bf01a09c38fa39359515ddb9d2d37a26bccb022968ef4c8e29a95c7c82edcbe561332ff79a51af"
        ),
        List.Cons(
          Branch(
            skip = 0,
            neighbors =
                hex"9d95e34e6f74b59d4ea69943d2759c01fe9f986ff0c03c9e25ab561b23a413b77792fa78d9fbcb98922a4eed2df0ed70a2852ae8dbac8cff54b9024f229e66629136cfa60a569c464503a8b7779cb4a632ae052521750212848d1cc0ebed406e1ba4876c4fd168988c8fe9e226ed283f4d5f17134e811c3b5322bc9c494a598b"
          ),
          List.Cons(
            Branch(
              skip = 0,
              neighbors =
                  hex"b93c3b90e647f90beb9608aecf714e3fbafdb7f852cfebdbd8ff435df84a4116d10ccdbe4ea303efbf0f42f45d8dc4698c3890595be97e4b0f39001bde3f2ad95b8f6f450b1e85d00dacbd732b0c5bc3e8c92fc13d43028777decb669060558821db21a9b01ba5ddf6932708cd96d45d41a1a4211412a46fe41870968389ec96"
            ),
            List.Cons(
              Branch(
                skip = 0,
                neighbors =
                    hex"f89f9d06b48ecc0e1ea2e6a43a9047e1ff02ecf9f79b357091ffc0a7104bbb260908746f8e61ecc60dfe26b8d03bcc2f1318a2a95fa895e4d1aadbb917f9f2936b900c75ffe49081c265df9c7c329b9036a0efb46d5bac595a1dcb7c200e7d590000000000000000000000000000000000000000000000000000000000000000"
              ),
              List.Nil
            )
          )
        )
      )
    )

    val trie = MerklePatriciaForestry(hex"225a4599b804ba53745538c83bfa699ecf8077201b61484c91171f5910a4a8f9")
    val blockHash = hex"0000000000000000000261a131bf48cc5a19658ade8cfede99dc1c3933300d60"
    val blockBody = hex"26f711634eb26999169bb927f629870938bb4b6b4d1a078b44a6b4ec54f9e8df"

    @Benchmark
    def insert(): ByteString = {
        trie.insert(blockHash, blockBody, proofBitcoin845602).root
    }
}
