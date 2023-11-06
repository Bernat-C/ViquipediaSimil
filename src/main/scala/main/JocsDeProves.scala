package main

object JocsDeProves {

  def prova1(folder: String): Unit = {
    Main.paginesMesRellevantsISimilars((folder, 4, 0, 16, 16))
  }

  def prova2(folder: String): Unit = {
    Main.paginesMesRellevantsISimilars((folder, 100, 0.5, 1, 1))
  }

  def prova3(folder: String): Unit = {
    Main.paginesMesRellevantsISimilars((folder, 100, 0.5, 16, 16))
  }

  def prova4(folder: String): Unit = {
    Main.paginesMesRellevantsISimilars((folder, 500, 0.5, 1, 1))
  }

  def prova5(folder: String): Unit = {
    Main.paginesMesRellevantsISimilars((folder, 500, 0.5, 16, 16))
  }

  def prova6(folder: String): Unit = {
    Main.similarNotRefGlobal((folder, 100, 0.5, 16, 16))
  }

  def prova7(folder: String): Unit = {
    Main.similarNotRefGlobal((folder, 500, 0.5, 16, 16))
  }

  def prova8(folder: String): Unit = {
    Main.meanReferences(folder, 16, 16)
  }

  def prova9(folder: String): Unit = {
    Main.meanImages(folder, 16, 16)
  }



}
