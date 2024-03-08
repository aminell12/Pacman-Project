package logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeSet;

import view.Gomme;
import data.*;

/**
 * class used to represent plan. It will provide for a given set of results an
 * action to perform in each result
 */
class Plans {
	ArrayList<Result> results;
	ArrayList<ArrayList<String>> actions;

	/**
	 * construct an empty plan
	 */
	public Plans() {
		this.results = new ArrayList<Result>();
		this.actions = new ArrayList<ArrayList<String>>();
	}

	/**
	 * add a new pair of belief-state and corresponding (equivalent) actions
	 *
	 * @param beliefBeliefState the belief state to add
	 * @param action            a list of alternative actions to perform. Only one
	 *                          of them is chosen but their results should be
	 *                          similar
	 */
	public void addPlan(Result beliefBeliefState, ArrayList<String> action) {
		this.results.add(beliefBeliefState);
		this.actions.add(action);
	}

	/**
	 * return the number of belief-states/actions pairs
	 *
	 * @return the number of belief-states/actions pairs
	 */
	public int size() {
		return this.results.size();
	}

	/**
	 * return one of the belief-state of the plan
	 *
	 * @param index index of the belief-state
	 * @return the belief-state corresponding to the index
	 */
	public Result getResult(int index) {
		return this.results.get(index);
	}

	/**
	 * return the list of actions performed for a given belief-state
	 *
	 * @param index index of the belief-state
	 * @return the set of actions to perform for the belief-state corresponding to
	 *         the index
	 */
	public ArrayList<String> getAction(int index) {
		return this.actions.get(index);
	}
}

/**
 * class used to represent a transition function i.e., a set of possible belief
 * states the agent may be in after performing an action
 */
class Result {
	private ArrayList<BeliefState> beliefStates;

	/**
	 * construct a new result
	 *
	 * @param states the set of states corresponding to the new belief state
	 */
	public Result(ArrayList<BeliefState> states) {
		this.beliefStates = states;
	}

	/**
	 * returns the number of belief states
	 *
	 * @return the number of belief states
	 */
	public int size() {
		return this.beliefStates.size();
	}

	/**
	 * return one of the belief state
	 *
	 * @param index the index of the belief state to return
	 * @return the belief state to return
	 */
	public BeliefState getBeliefState(int index) {
		return this.beliefStates.get(index);
	}

	/**
	 * return the list of belief-states
	 *
	 * @return the list of belief-states
	 */
	public ArrayList<BeliefState> getBeliefStates() {
		return this.beliefStates;
	}
}

/**
 * class implement the AI to choose the next move of the Pacman
 */
public class AI {
	/**
	 * function that compute the next action to do (among UP, DOWN, LEFT, RIGHT)
	 *
	 * @param beliefState the current belief-state of the agent
	 * @param deepth      the deepth of the search (size of the largest sequence of
	 *                    action checked)
	 * @return a string describing the next action (among
	 *         PacManLauncher.UP/DOWN/LEFT/RIGHT)
	 */

	/* Partie Générale */

	/**
	 * Calculates the Manhattan distance between two positions.
	 *
	 * @param pos1 The first position.
	 * @param pos2 The second position.
	 * @return The Manhattan distance between the two positions.
	 */
	private static int calculateDistanceMan(Position pos1, Position pos2) {
		return Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y);
	}

	/**
	 * Checks if the next move of PacMan is valid ie it is not a wall.
	 *
	 * @param beliefState The current belief state of the agent.
	 * @param move        The move to validate.
	 * @return true if the move is valid, false otherwise.
	 */
	private static boolean isValidMove(BeliefState beliefState, String move) {
		// Créer une copie de la position actuelle du Pacman
		Position currentPosition = beliefState.getPacmanPos().clone();
		// Mise à jour de la position temporaire en fonction du mouvement
		switch (move) {
			case "UP":
				currentPosition.x -= 1;
				break;
			case "DOWN":
				currentPosition.x += 1;
				break;
			case "LEFT":
				currentPosition.y -= 1;
				break;
			case "RIGHT":
				currentPosition.y += 1;
				break;
		}
		// Utiliser la position temporaire pour vérifier si le mouvement est valide
		int x = currentPosition.x;
		int y = currentPosition.y;
		if (x >= 0 && y >= 0) {

			// Si c'est un mur, le mouvement n'est pas valide
			if (beliefState.getMap(x, y) == '#') {
				return false;
			}
		}
		return true; // Si ce n'est pas un mur, le mouvement est valide
	}

	/**
	 * Finds the position of the closest gomme.
	 *
	 * @param beliefState The current belief state of the agent.
	 * @return The position of the closest gomme.
	 */
	private static Position findClosestGomme(BeliefState beliefState) {
		// clone la position actuelle du PacMan à partir du BeliefState
		Position pacmanPosition = beliefState.getPacmanPos().clone();
		// Liste pour avoir toutes les positions des gommes dans la map actuelle
		ArrayList<Position> gommes = new ArrayList<>();
		// Collecte toutes les positions des gommes
		for (int x = 2; x < 25; x++) {
			for (int y = 2; y < 25; y++) {
				// '.' représente une gomme '*' une super gomme
				if (beliefState.getMap(x, y) == '.' || beliefState.getMap(x, y) == '*') {
					gommes.add(new Position(x, y, 'U'));
				}
			}
		}
		// Recherche de la gomme la plus proche en utilisant l'algorithme A*
		Position closestGomme = null; // stocke la position de la gomme la plus proche
		int min_distance = Integer.MAX_VALUE; // stocke la distance minimale trouvée jusqu'à présent
		for (Position gommePos : gommes) {
			// Utilisez l'algorithme A* pour calculer le coût total du chemin jusqu'à cette
			// gomme
			// calcule la distance de Manhattan entre la position du PacMan et la position
			// de la gomme
			int min_dis = calculateDistanceMan(gommePos, pacmanPosition);

			if (min_dis < min_distance) {
				closestGomme = gommePos;
				min_distance = min_dis;
			}
		}
		return closestGomme;
	}

	/**
	 * Computes and returns a possible position based on the current movement.
	 *
	 * @param currentPosition The current position.
	 * @param move            The movement to perform (UP, DOWN, LEFT, RIGHT).
	 * @return The new position after applying the movement.
	 */
	private static Position getNextPosition(Position currentPosition, String move) {
		// Créez une copie de la position actuelle
		Position newPosition = currentPosition.clone();
		// Mise à jour de la position en fonction du mouvement
		switch (move) {
			case "UP":
				newPosition.x -= 1;
				break;
			case "DOWN":
				newPosition.x += 1;
				break;
			case "LEFT":
				newPosition.y -= 1;
				break;
			case "RIGHT":
				newPosition.y += 1;
				break;
		}
		return newPosition;
	}

	/**
	 * Modifies the risk level at a specific position in the risk grid.
	 *
	 * @param x         The index of the row in the risk grid.
	 * @param y         The index of the column in the risk grid.
	 * @param riskValue The value of the risk to be added.
	 * @param nb_pos    The number of potential positions for one of the ghosts
	 */
	private static void updateRiskAtPosition(int x, int y, int riskValue, int nb_pos) {
		if (x >= 0 && x < RiskCount.length && y >= 0 && y < RiskCount[x].length) {
			RiskCount[x][y] += riskValue / nb_pos;
		}
	}

	/**
	 * Applies a specific risk pattern around a given position of a ghost.
	 *
	 * @param pos    The position of the potential ghost around which to apply the
	 *               risk pattern.
	 * @param nb_pos The number of potential positions of the ghost (used to adjust
	 *               the risk).
	 * @param init   Boolean to initialize or increase the risk.
	 */
	private static void applyRiskPattern(Position pos, int nb_pos, boolean init) {
		// Quand le boolean est false, quand on veut mettre a jour les risques du pacman
		if (!init) {
			// Appliquer le motif de risque spécifique autour de chaque position de fantôme
			// Au niveau de la position du fantome
			updateRiskAtPosition(pos.x, pos.y, 500, nb_pos);
			// les cases juste a cote de la position du fantome suppose
			updateRiskAtPosition(pos.x - 1, pos.y, 500, nb_pos);
			updateRiskAtPosition(pos.x, pos.y - 1, 500, nb_pos);
			updateRiskAtPosition(pos.x + 1, pos.y, 500, nb_pos);
			updateRiskAtPosition(pos.x, pos.y + 1, 500, nb_pos);
			// Diagonales autour du fantomes
			updateRiskAtPosition(pos.x - 1, pos.y - 1, 100, nb_pos);
			updateRiskAtPosition(pos.x - 1, pos.y + 1, 100, nb_pos);
			updateRiskAtPosition(pos.x + 1, pos.y + 1, 100, nb_pos);
			updateRiskAtPosition(pos.x + 1, pos.y - 1, 100, nb_pos);

			// Case en plus
			updateRiskAtPosition(pos.x - 2, pos.y - 1, 40, nb_pos);
			updateRiskAtPosition(pos.x - 2, pos.y + 1, 40, nb_pos);
			updateRiskAtPosition(pos.x + 1, pos.y + 2, 40, nb_pos);
			updateRiskAtPosition(pos.x + 1, pos.y - 2, 40, nb_pos);
			updateRiskAtPosition(pos.x - 1, pos.y - 2, 40, nb_pos);
			updateRiskAtPosition(pos.x - 1, pos.y + 2, 40, nb_pos);
			updateRiskAtPosition(pos.x + 2, pos.y + 1, 40, nb_pos);
			updateRiskAtPosition(pos.x + 2, pos.y - 1, 40, nb_pos);

			// Autour du fantôme, verticalement et horizontalement
			for (int i = 2; i <= 3; i++) {
				// Si on est 3 cases a cote on met le risque a 50 sinon le risque est mis a 100
				updateRiskAtPosition(pos.x - i, pos.y, (i == 3) ? 50 : 100, nb_pos);
				updateRiskAtPosition(pos.x + i, pos.y, (i == 3) ? 50 : 100, nb_pos);
				updateRiskAtPosition(pos.x, pos.y - i, (i == 3) ? 50 : 100, nb_pos);
				updateRiskAtPosition(pos.x, pos.y + i, (i == 3) ? 50 : 100, nb_pos);
			}
		}
		// Dans ce bloc on reinitialise les cases autour du pacman
		else {
			// Au niveau de la position du fantome
			InitRiskAtPosition(pos.x, pos.y, 500, nb_pos);
			// les cases juste a cote de la position du fantome suppose
			InitRiskAtPosition(pos.x - 1, pos.y, 500, nb_pos);
			InitRiskAtPosition(pos.x, pos.y - 1, 500, nb_pos);
			InitRiskAtPosition(pos.x + 1, pos.y, 500, nb_pos);
			InitRiskAtPosition(pos.x, pos.y + 1, 500, nb_pos);
			// Diagonales autour du fantomes
			InitRiskAtPosition(pos.x - 1, pos.y - 1, 100, nb_pos);
			InitRiskAtPosition(pos.x - 1, pos.y + 1, 100, nb_pos);
			InitRiskAtPosition(pos.x + 1, pos.y + 1, 100, nb_pos);
			InitRiskAtPosition(pos.x + 1, pos.y - 1, 100, nb_pos);
			// Case en plus
			InitRiskAtPosition(pos.x - 2, pos.y - 1, 40, nb_pos);
			InitRiskAtPosition(pos.x - 2, pos.y + 1, 40, nb_pos);
			InitRiskAtPosition(pos.x + 1, pos.y + 2, 40, nb_pos);
			InitRiskAtPosition(pos.x + 1, pos.y - 2, 40, nb_pos);
			InitRiskAtPosition(pos.x - 1, pos.y - 2, 40, nb_pos);
			InitRiskAtPosition(pos.x - 1, pos.y + 2, 40, nb_pos);
			InitRiskAtPosition(pos.x + 2, pos.y + 1, 40, nb_pos);
			InitRiskAtPosition(pos.x + 2, pos.y - 1, 40, nb_pos);

			for (int i = 2; i <= 3; i++) {
				InitRiskAtPosition(pos.x - i, pos.y, (i == 3) ? 50 : 100, nb_pos);
				InitRiskAtPosition(pos.x + i, pos.y, (i == 3) ? 50 : 100, nb_pos);
				InitRiskAtPosition(pos.x, pos.y - i, (i == 3) ? 50 : 100, nb_pos);
				InitRiskAtPosition(pos.x, pos.y + i, (i == 3) ? 50 : 100, nb_pos);
			}
		}
	}

	/**
	 * Updates the risk grid based on the position of the ghosts.
	 *
	 * @param beliefState The current belief state of the agent.
	 */
	private static void updateRiskGrid(BeliefState beliefState) {
		// Recupere toutes les positions des deux fantomes
		TreeSet<Position> positionsFantome1 = beliefState.getGhostPositions(0);
		TreeSet<Position> positionsFantome2 = beliefState.getGhostPositions(1);
		// Si le pacman pense qu'il y a le fantome1 a un certain endroit on augmente les
		// cases autour de cet endroit pour eviter
		// qu'il se rapproche du fantome
		for (Position pos : positionsFantome1) {
			applyRiskPattern(pos, positionsFantome1.size(), false);
		}
		// Si le pacman pense qu'il y a le fantome1 a un certain endroit on augmente les
		// cases autour de cet endroit pour eviter
		// qu'il se rapproche du fantome
		for (Position pos : positionsFantome2) {
			applyRiskPattern(pos, positionsFantome2.size(), false);
		}
	}

	/**
	 * Resets the risk level at a specific position in the grid.
	 *
	 * @param x         The index of the row in the risk grid.
	 * @param y         The index of the column in the risk grid.
	 * @param riskValue The value of the risk to subtract.
	 * @param nb_pos    The number of positions (used to divide the risk).
	 */
	private static void InitRiskAtPosition(int x, int y, int riskValue, int nb_pos) {
		if (x >= 0 && x < RiskCount.length && y >= 0 && y < RiskCount[x].length) {
			RiskCount[x][y] -= riskValue / nb_pos;
		}
	}

	/**
	 * Resets the risk grid based on the position of the ghosts.
	 *
	 * @param beliefState The current belief state of the agent.
	 */
	private static void InitRiskGrid(BeliefState beliefState) {
		// Mettre à jour le tableau avec les nouveaux risques
		TreeSet<Position> positionsFantome1 = beliefState.getGhostPositions(0);
		TreeSet<Position> positionsFantome2 = beliefState.getGhostPositions(1);
		// Si le pacman voit le fantome1 on augmente les cases autour de lui pour eviter
		// qu'il se rapproche du fantome
		for (Position pos : positionsFantome1) {
			applyRiskPattern(pos, positionsFantome1.size(), true);
		}

		for (Position pos : positionsFantome2) {
			applyRiskPattern(pos, positionsFantome2.size(), true);
		}
	}

	/**
	 * Modifies the risk grid based on the presence of gommes.
	 *
	 * @param beliefState The current belief state of the agent.
	 */
	private static void updateRiskGridGomme(BeliefState beliefState) {
		for (int x = 2; x < 25; x++) {
			for (int y = 2; y < 25; y++) {
				char cell = beliefState.getMap(x, y);
				if (cell == '*') { // si il y a une super gomme
					RiskCount[x][y] = -50;
				}
				if (cell == '.') { // si y a une gomme simple
					RiskCount[x][y] = -20;
				}
			}
		}
	}

	/**
	 * Updates the risk matrix based on the ghost positions provided by the belief
	 * state.
	 *
	 * @param beliefState The current belief state of the agent.
	 */
	private static void attackFantome(BeliefState beliefState, int numfantome) {
		if (numfantome == 0) {
			// Mettre à jour le tableau avec les nouveaux risques
			TreeSet<Position> positionsFantome1 = beliefState.getGhostPositions(0);
			for (Position posi : positionsFantome1) {
				ArrayList<Position> path = shortestPathBFS(beliefState, posi);
				for (Position pos : path) {
					RiskCount[pos.x][pos.y] -= (250 / positionsFantome1.size());
				}
			}
		}
		if (numfantome == 1) {
			// Si le pacman voit le fantome1 on augmente les cases autour de lui pour eviter
			// qu'il se rapproche du fantome
			TreeSet<Position> positionsFantome2 = beliefState.getGhostPositions(1);
			for (Position posi : positionsFantome2) {
				ArrayList<Position> path2 = shortestPathBFS(beliefState, posi);
				for (Position pos : path2) {
					RiskCount[pos.x][pos.y] -= (250 / positionsFantome2.size());
				}
			}
		}
	}

	/**
	 * Initializes the risk assessment by updating the risk matrix based on the
	 * ghost positions
	 *
	 * @param beliefState The current belief state of the agent.
	 */
	private static void init_attackFantome(BeliefState beliefState, int numfantome) {
		if (numfantome == 0) {
			// Mettre à jour le tableau avec les nouveaux risques
			TreeSet<Position> positionsFantome1 = beliefState.getGhostPositions(0);
			for (Position posi : positionsFantome1) {
				ArrayList<Position> path = shortestPathBFS(beliefState, posi);
				for (Position pos : path) {
					RiskCount[pos.x][pos.y] += (250 / positionsFantome1.size());
				}
			}
		}
		if (numfantome == 1) {
			// Si le pacman voit le fantome1 on augmente les cases autour de lui pour eviter
			// qu'il se rapproche du fantome
			TreeSet<Position> positionsFantome2 = beliefState.getGhostPositions(1);
			for (Position posi : positionsFantome2) {
				ArrayList<Position> path2 = shortestPathBFS(beliefState, posi);
				for (Position pos : path2) {
					RiskCount[pos.x][pos.y] += (250 / positionsFantome2.size());
				}
			}
		}
	}

	/**
	 * Helper method to construct a path towards a goal.
	 *
	 * @param parentMap A map tracking parent positions for each position.
	 * @param path      The path to construct.
	 * @param current   The current position for which to build the path.
	 */
	private static void constructPath(HashMap<Position, Position> parentMap, ArrayList<Position> path,
			Position current) {
		while (current != null) {
			path.add(0, current);
			current = parentMap.get(current);
		}
	}

	/**
	 * Computes and returns the shortest path to a specific goal by using BFS.
	 *
	 * @param beliefState The current belief state of the agent.
	 * @param goal        The goal to find the shortest path towards.
	 * @return A list of positions representing the shortest path.
	 */
	private static ArrayList<Position> shortestPathBFS(BeliefState beliefState, Position goal) {
		// Initialise une file de priorite basée sur la distance de Manhattan jusqu'a
		// l'objectif
		PriorityQueue<Position> queue = new PriorityQueue<>(
				Comparator.comparingInt(p -> calculateDistanceMan(p, goal)));
		// Map pour suivre les positions parentes pour chaque position dans le chemin
		HashMap<Position, Position> parentMap = new HashMap<>();
		// Liste pour stocker le chemin
		ArrayList<Position> path = new ArrayList<>();
		// Tableau pour marquer les positions visitées dans la grille
		boolean[][] visited = new boolean[30][30];
		// Obtenir la position de départ (position de Pacman)
		Position start = beliefState.getPacmanPos().clone();
		// ajoute la pisition de depart a la file
		queue.add(start);
		parentMap.put(start, null);
		// Utilisation de BFS pour trouver le plus court chemin
		while (!queue.isEmpty()) {
			// enleve l'element de la file de priorite
			Position current = queue.poll();
			int x = current.x;
			int y = current.y;
			// Vérifier si la position actuelle a été visitée
			if (visited[x][y]) {
				// si oui on prend le prochain element dans la file
				continue;
			}
			// Marquer la position actuelle comme visitée
			visited[x][y] = true;
			// Vérifier si la position objectif a été atteinte
			if (x == goal.x && y == goal.y) {
				// si oui on onstruit le chemin en utilisant parentMap
				constructPath(parentMap, path, current);
				// retourne le chemin allant a cet etat but
				return path;
			}
			// Sinon on genere les mouvements possibles à partir de la position actuelle
			int[] dx = { 1, -1, 0, 0 };
			int[] dy = { 0, 0, 1, -1 };
			for (int i = 0; i < 4; i++) {
				// Vérifier les limites
				if ((current.x == 0 && dx[i] == -1) || (current.y == 0 && dy[i] == -1))
					continue;
				// si on est pas dans les limites du tableau
				int newX = current.x + dx[i];
				int newY = current.y + dy[i];
				Position nextMove = new Position(newX, newY, 'U');
				// evite les mouvement menant a des murs
				if (beliefState.getMap(newX, newY) != '#') {
					// Ajouter les mouvements valides à la file et mettre à jour parentMap
					queue.add(nextMove);
					parentMap.put(nextMove, current);
				}
			}
		}
		// Retourner un chemin vide si l'objectif est impossible
		return new ArrayList<>();
	}

	/* Variables */

	/**
	 * Array representing risk levels for each position on the map.
	 * Each cell in the array stores a risk score, influencing PacMan's movement
	 * decisions.
	 * Higher scores indicate higher risk, while lower scores indicate lower risk.
	 */
	private static int[][] RiskCount = new int[30][30];

	/**
	 * List storing the positions forming the shortest path to the nearest gomme.
	 * Used to guide PacMan efficiently towards the gomme.
	 */
	static ArrayList<Position> shortestPath;

	/**
	 * Counter used to determine when to reset the calculation for the nearest
	 * gomme.
	 * When "cpt" reaches 6, it triggers a recalculation to determine which gomme is
	 * the closest,
	 * thereby avoiding recalculating with every individual movement.
	 */
	private static int cpt = 0;

	/**
	 * Finds and returns the next move for PacMan.
	 *
	 * @param beliefState The current belief state of the agent.
	 * @return A string describing the next move (UP, DOWN, LEFT, RIGHT).
	 */
	public static String findNextMove(BeliefState beliefState) {
		// Position du Pacman
		Position currentPosition = beliefState.getPacmanPos().clone();
		// Récupère la position de la gomme la plus proche
		Position closestGomme = findClosestGomme(beliefState);
		// On augmente le risque sur la position du PacMan pour qu'il evite de revenir
		// sur son chemin
		RiskCount[currentPosition.x][currentPosition.y] += 15;
		// Niveau de peur des fantomes
		int NiveauPeur_fantome1 = beliefState.getCompteurPeur(0);
		int NiveauPeur_fantome2 = beliefState.getCompteurPeur(1);
		// Nombre de gomme dans la map actuelle
		int NbGomme = beliefState.getNbrOfGommes();
		// Initialise le tableau des risques à chaque mouvement en fonction de la
		// postion du fantôme1 lorsque celui ci est pas ou tres peu effrayee
		if (NiveauPeur_fantome1 < 10) {
			updateRiskGrid(beliefState);
		}
		// Initialise le tableau des risques à chaque mouvement en fonction de la
		// postion du fantôme2 lorsque celui ci est pas ou tres peu effrayee
		if (NiveauPeur_fantome2 < 10) {
			updateRiskGrid(beliefState);
		}
		// Si les deux fantômes sont en état de peur en même temps le pacman pass en
		// attaque
		if ((NiveauPeur_fantome1 >= 10)) {
			// mise en attaque de notre pacman vers le fantôme 1
			attackFantome(beliefState, 0);
		}

		if ((NiveauPeur_fantome2 >= 10)) {
			// mise en attaque de notre pacman vers le fantôme 2
			attackFantome(beliefState, 1);
		}

		// On modifie le tableau des risques en fonction des gommes (Si une gomme est
		// proche on diminue le risque)
		if (NbGomme == 140 || NbGomme == 205 || NbGomme == 165) {
			updateRiskGridGomme(beliefState);
		}
		// Permet de voir s'il a mangé la gomme ou pas et d'aller vers l'autre gomme la
		// plus proche
		if (beliefState.getMap(closestGomme.x, closestGomme.y) == 'O' || cpt > 4) {
			cpt = 0;
			closestGomme = findClosestGomme(beliefState);
		}
		cpt++;
		shortestPath = shortestPathBFS(beliefState, closestGomme);
		for (Position path : shortestPath) {
			// Si les deux fantomes ont peur en même temps
			if (beliefState.getCompteurPeur(0) >= 10 && beliefState.getCompteurPeur(1) >= 10) {
				RiskCount[path.x][path.y] -= 200;
			}
			RiskCount[path.x][path.y] -= 25;
		}
		// Évaluez le risque pour chaque direction possible
		Plans pP = beliefState.extendsBeliefState();
		String bestAction = null;
		int minRisk = Integer.MAX_VALUE;
		// On évalue le risque pour chaque action possible et on renvoie le risque
		// associé
		for (int i = 0; i < pP.size(); i++) {
			ArrayList<String> actions = pP.getAction(i);
			for (String move : actions) {
				if (isValidMove(beliefState, move)) {
					// Calculez une position hypothétique après le mouvement
					Position nextPos = getNextPosition(currentPosition, move);
					int risk = RiskCount[nextPos.x][nextPos.y];
					if (risk < minRisk) {
						minRisk = risk;
						bestAction = move;
					}
				}
			}
		}
		/* Parite pour réinitialiser le tableau des risques du pacman */
		// Initialise le tableau des risques à chaque mouvement en fonction de la
		// postion du fantôme1
		if (NiveauPeur_fantome1 < 10) {
			InitRiskGrid(beliefState);
		}
		if (NiveauPeur_fantome2 < 10) {
			InitRiskGrid(beliefState);
		}
		// On enlève le mode attaque après avoir trouvé le bestmove en réinitialisant le
		// tableau des risques
		if ((NiveauPeur_fantome1 >= 10)) {
			init_attackFantome(beliefState, 0);
		}
		if ((NiveauPeur_fantome2 >= 10)) {
			init_attackFantome(beliefState, 1);
		}

		for (Position path : shortestPath) {
			// Si les deux fantomes ont peur en même temps
			if (beliefState.getCompteurPeur(0) >= 10 && beliefState.getCompteurPeur(1) >= 10) {
				RiskCount[path.x][path.y] += 200;
			}
			RiskCount[path.x][path.y] += 25;
		}
		// Si le niveau ne contient plus de gomme on arrete
		if (beliefState.getNbrOfGommes() == 0) {
			return null;
		}
		return bestAction != null ? bestAction : "STOP";
	}
}
