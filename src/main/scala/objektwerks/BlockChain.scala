package objektwerks

import Entity.dateTimeMillis
import Hash.Hash
import ProofOfWork.ProofOfWork

sealed trait Entity extends Product with Serializable
object Entity {
  import java.sql.Timestamp
  import java.util.Date

  def dateTimeMillis: Long = new Timestamp( new Date().getTime ).getTime
}

final case class Block[T](timestamp: Long,
                          hash: Hash,
                          previousHash: Hash,
                          proofOfWork: ProofOfWork,
                          value: T) extends Entity

object Block {
  def apply[T](previousHash: Hash, value: T): Block[T] = {
    val timestamp = dateTimeMillis
    val hash = Hash.sha3256( timestamp.toString + previousHash + value.toString )
    val proofOfWork = ProofOfWork.solve(hash)
    Block[T](timestamp, hash, previousHash, proofOfWork, value)
  }
}

final case class HashBlock[T](hash: Hash, block: Block[T]) extends Entity

final case class BlockChain[T](genesisBlock: Block[T]) extends Entity {
  import scala.collection.mutable

  val timestamp: Long = dateTimeMillis
  private val chain = mutable.LinkedHashMap.empty[Hash, Block[T]]
  chain += genesisBlock.hash -> genesisBlock

  def hash: String = Hash.sha3256( chain.keys.fold( dateTimeMillis.toString )(_ + _) )

  def last: HashBlock[T] = toHashBlock( chain.last )

  def add(block: Block[T]): Boolean =
    if ( !chain.contains(block.hash) && ( block.previousHash == last.hash ) ) {
      chain += block.hash -> block
      true
    } else false

  def get(hash: Hash): Option[Block[T]] = chain.get(hash)

  def list: Map[Hash, Block[T]] = chain.toMap

  private def toHashBlock(tuple: (Hash, Block[T])): HashBlock[T] = HashBlock( tuple._1, tuple._2 )
}