package br.ufjf.coordenacao.sistemagestaocurso.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import br.ufjf.coordenacao.OfertaVagas.model.Class;
import br.ufjf.coordenacao.OfertaVagas.model.ClassStatus;
import br.ufjf.coordenacao.OfertaVagas.model.Curriculum;
import br.ufjf.coordenacao.OfertaVagas.model.Student;
import br.ufjf.coordenacao.OfertaVagas.model.StudentsHistory;
import br.ufjf.coordenacao.sistemagestaocurso.model.estrutura.SituacaoDisciplina;
import br.ufjf.coordenacao.sistemagestaocurso.repository.DisciplinaRepository;
import br.ufjf.coordenacao.sistemagestaocurso.repository.EventoAceRepository;
import br.ufjf.coordenacao.sistemagestaocurso.util.arvore.*;

@Entity
@SequenceGenerator(name="aluno_sequencia", sequenceName="aluno_seq", allocationSize=1)  
public class Aluno {
	// ==========================VARI√ÉÔøΩVEIS=================================================================================================================//

	private Long id;
	private String matricula;
	private Curso curso;
	private Grade grade;
	private String nome;
	private Float ira;
	private Integer periodoReal;
	private List<Historico> grupoHistorico;
	private List<IRA> iras;
	private int horasObrigatoriasCompletadas;
	private int horasEletivasCompletadas;
	private int sobraHorasEletivas;
	private int horasOpcionaisCompletadas;
	private int horasAceConcluidas;
	private int sobraHorasOpcionais;

	private boolean horasCalculadas;
	
	private DisciplinaRepository disciplinaRepository;
	private EventoAceRepository eventoAceRepository;

	// ==========================GETTERS_AND_SETTERS======================================================================================================//

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY, generator="aluno_sequencia")  
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}

	@Column (name="MATRICULA", unique = true, nullable = false)
	public String getMatricula() {
		return matricula;
	}

	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}


	@Column(name="NOME")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}	

	@Transient
	public String getPeriodoIngresso() {

		String periodoInicioLocal = "";
		if (matricula != null){
			periodoInicioLocal =  matricula.substring(0,4) ;
		}		
		return periodoInicioLocal;
	}

	@ManyToOne
	@JoinColumn(name="ID_CURSO" , referencedColumnName="ID")
	public Curso getCurso() {
		return curso;
	}

	public void setCurso(Curso curso) {
		this.curso = curso;
	}

	@ManyToOne
	@JoinColumn(name="ID_GRADE" , referencedColumnName="ID",nullable = false)
	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}
	
	@OneToMany(mappedBy = "aluno", targetEntity = IRA.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<IRA> getIras(){
		return iras;
	}

	@OneToMany(mappedBy = "aluno", targetEntity = Historico.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	public List<Historico> getGrupoHistorico() {
		return grupoHistorico;
	}
	

	@Transient
	public List<Historico> getGrupoHistorico(String statusDisciplina) {
		List<Historico> historicos = new ArrayList<Historico>();
		for(Historico h : this.getGrupoHistorico()) {
			if(statusDisciplina.equals(h.getStatusDisciplina()))
				historicos.add(h);
		}
		return historicos;
	}

	public void setGrupoHistorico(List<Historico> grupoHistorico) {
		this.grupoHistorico = grupoHistorico;
	}

	@Transient
	public Integer getPeriodoCorrente(String periodoAtual) {

		String periodoInicioLocal;
		if (grade.getPeriodoInicio() == 0){
			periodoInicioLocal = matricula.substring(0,4) + "1";
		}
		else {
			periodoInicioLocal =  matricula.substring(0,4)  + String.valueOf(grade.getPeriodoInicio());
		}
		return periodoCorrente( periodoInicioLocal,periodoAtual);
	}

	@Transient
	public Integer getPeriodoReal() {
		return periodoReal;
	}

	public void setPeriodoReal(Integer periodoReal) {
		this.periodoReal = periodoReal;
	}

	@Column(name="IRA")
	public Float getIra() {
		return ira;
	}


	public void setIra(Float ira) {
		this.ira = ira;
	}

	public int periodoCorrente(String ingresso,String semestreAtual){
		
		int i = 1;
		
		int anoAtual = Integer.parseInt(semestreAtual.substring(0, 4));
		int periodoAtual = Integer.parseInt(semestreAtual.substring(4, 5));
		
		int anoIngresso = Integer.parseInt(ingresso.substring(0, 4));
		int periodoIngresso = Integer.parseInt(ingresso.substring(4, 5));
		while( anoIngresso != anoAtual || periodoAtual != periodoIngresso  ){
			i++;
			if (periodoIngresso == 3){
				anoIngresso++;
				periodoIngresso = 1;
			}
			else{
				periodoIngresso = 3;
			}
		}
		return i;
	}

	@Transient
	public String getTurma() {

		return matricula.substring(0,4);
	}
	
	@Transient
	public void calculaHorasCompletadas()
	{
		/*ContadorHorasIntegralizadas contador = new ContadorHorasIntegralizadas(this);
		this.horasAceConcluidas = contador.getHorasAceConcluidas();
		this.horasEletivasCompletadas = contador.getHorasEletivasCompletadas();
		this.horasObrigatoriasCompletadas = contador.getHorasObrigatoriasCompletadas();
		this.horasOpcionaisCompletadas = contador.getHorasOpcionaisCompletadas();
		this.sobraHorasEletivas = contador.getSobraHorasEletivas();
		this.sobraHorasOpcionais = contador.getSobraHorasOpcionais();
		contador.dispose();
		*/

		this.horasObrigatoriasCompletadas = 0;
		this.horasEletivasCompletadas = 0;
		this.horasOpcionaisCompletadas = 0;
		this.sobraHorasEletivas = 0;
		this.horasCalculadas = false;
		
		ImportarArvore importador;
		EstruturaArvore estruturaArvore = EstruturaArvore.getInstance();
		importador = estruturaArvore.recuperarArvore(this.grade,false);
		
		Curriculum cur = importador.get_cur();
		StudentsHistory sh = importador.getSh();		
		Student st = sh.getStudents().get(this.getMatricula());
		
		if (st == null){
			FacesMessage msg = new FacesMessage("O aluno:" + this.getMatricula() + " não tem nenhum histórico de matricula cadastrado!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			return;
		}
		
		HashMap<Class, ArrayList<String[]>> aprovado = new HashMap<Class, ArrayList<String[]>>(st.getClasses(ClassStatus.APPROVED));
		
		for(int i: cur.getMandatories().keySet())
		{
			for(Class c: cur.getMandatories().get(i))
			{
				if(aprovado.containsKey(c)){
					
					Disciplina d = disciplinaRepository.buscarPorCodigoDisciplina(c.getId());
					if(d != null)
					{
						c.setWorkload(d.getCargaHoraria());
					}
					
					this.horasObrigatoriasCompletadas += c.getWorkload();
					aprovado.remove(c);
				}
				
				
			}
		}
		
		for(Class c: cur.getElectives())
			if(aprovado.containsKey(c))	{
				this.horasEletivasCompletadas += c.getWorkload();
				aprovado.remove(c);
			}
		
		Set<Class> ap = aprovado.keySet();
		Iterator<Class> i = ap.iterator();
		while(i.hasNext()){
			Class c = i.next();
			for(String[] s2: aprovado.get(c))	{
				if (s2[1].equals("APR") || s2[1].equals("A")){
					Disciplina d = disciplinaRepository.buscarPorCodigoDisciplina(c.getId());
					
					if(d != null)
						c.setWorkload(d.getCargaHoraria());
					
					horasAceConcluidas += c.getWorkload();
				}
				else
				{
					Disciplina opcional = disciplinaRepository.buscarPorCodigoDisciplina(c.getId());
					horasOpcionaisCompletadas += opcional.getCargaHoraria();
				}
			}
		}
		
		if(this.horasEletivasCompletadas > this.grade.getHorasEletivas())
		{
			this.sobraHorasEletivas = this.horasEletivasCompletadas - this.grade.getHorasEletivas();
			this.horasEletivasCompletadas -= this.sobraHorasEletivas;
			this.horasOpcionaisCompletadas += this.sobraHorasEletivas;
		}
		
		if(this.horasOpcionaisCompletadas > this.grade.getHorasOpcionais())
		{
			this.sobraHorasOpcionais = this.horasOpcionaisCompletadas - this.grade.getHorasOpcionais();
			this.horasOpcionaisCompletadas -= this.sobraHorasOpcionais;
		}
		
		this.horasCalculadas = true;
	}
	
	@Transient
	public int getHorasObrigatoriasCompletadas() {
		if(!this.horasCalculadas)
			this.calculaHorasCompletadas();
			
		return this.horasObrigatoriasCompletadas;
	}

	@Transient
	public int getHorasEletivasCompletadas() {
		if(!this.horasCalculadas)
			this.calculaHorasCompletadas();
		
		return this.horasEletivasCompletadas;
	}

	@Transient
	public int getHorasOpcionaisCompletadas() {
		if(!this.horasCalculadas)
			this.calculaHorasCompletadas();
		
		return this.horasOpcionaisCompletadas;
	}
	
	@Transient
	public int getSobraHorasEletivas()
	{
		if(!this.horasCalculadas)
			this.calculaHorasCompletadas();
		
		return this.sobraHorasEletivas;
	}
	
	@Transient
	public int getHorasAceConcluidas() {		
		if(!this.horasCalculadas)
			this.calculaHorasCompletadas();
		
		int aceCadastradas = 0;
		
		List<EventoAce> eventos = this.eventoAceRepository.buscarPorMatricula(this.getMatricula());
		
		for(EventoAce evento: eventos)
		{
			aceCadastradas += evento.getHoras();
		}
				
		this.horasAceConcluidas =  Math.min((horasAceConcluidas + aceCadastradas), this.grade.getHorasAce());
		
		return this.horasAceConcluidas;
	}
	
	@Transient
	public int getSobraHorasOpcionais()
	{
		if(!this.horasCalculadas)
			this.calculaHorasCompletadas();
		
		return this.sobraHorasOpcionais;
	}
	
	@Transient
	public void dadosAlterados()
	{
		this.horasCalculadas = false;
	}


	public void setIras(List<IRA> iras) {
		this.iras = iras;
	}
	
	@Transient
	public SituacaoDisciplina getExcedenteEletivas() {
		SituacaoDisciplina disciplinaSituacao = new SituacaoDisciplina();
		disciplinaSituacao.setCodigo("");
		disciplinaSituacao.setSituacao("");
		disciplinaSituacao.setCargaHoraria(this.getSobraHorasEletivas() + "");
		disciplinaSituacao.setNome("EXCEDENTE EM DISCIPLINAS ELETIVAS");
		return disciplinaSituacao;
	}
	
	@Transient
	public EventoAce getExcedenteOpcionais() {
		EventoAce evento = new EventoAce();
		evento.setDescricao("EXCEDENTE EM DISCIPLINAS OPCIONAIS");
		evento.setHoras((long)this.getSobraHorasOpcionais());
		evento.setExcluir(false);
		return evento;
	}
	
	@Transient
	public void setEventoAceRepository(EventoAceRepository eventoAceRepository) {
		this.eventoAceRepository = eventoAceRepository;
	}
	
	@Transient
	public void setDisciplinaRepository(DisciplinaRepository disciplinaRepository) {
		this.disciplinaRepository = disciplinaRepository;
	}
}